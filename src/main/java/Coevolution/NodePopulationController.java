package coevolution;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

import org.apache.commons.lang3.SerializationUtils;

import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.engine.network.activation.ActivationSigmoid;

public class NodePopulationController
implements Runnable
{
	private ArrayList<Node[]> networkedPopulation[];
	private double[] linearRankTable = new double[NodeConfig.populationSize];

	public void evolutionaryTick()
	{	
		for (int layer = 0; layer < NodeConfig.layerSizes.length-1; layer++)
			for (int populationInLayer=0; populationInLayer<NodeConfig.layerSizes[layer]; populationInLayer++)
			{
				Arrays.sort(networkedPopulation[layer].get(populationInLayer));
				populationEvolutionaryTick(layer, populationInLayer);
			}
	}

	public NodePopulationController()
	{
		networkedPopulation = (ArrayList<Node[]>[]) new ArrayList[NodeConfig.layerSizes.length-1];
		setupLinearRankTable();

		for (int layer=0; layer<NodeConfig.layerSizes.length-1; layer++)
		{
			networkedPopulation[layer] = new ArrayList<>(NodeConfig.layerSizes[layer]);
			for (int nodePopulationCount=0; nodePopulationCount<NodeConfig.layerSizes[layer]; nodePopulationCount++)
			{
				Node[] nodePopulation = new Node[NodeConfig.populationSize	];
				for (int i=0; i<NodeConfig.populationSize; i++)
					nodePopulation[i] = new Node(NodeConfig.layerSizes[layer+1], layer, nodePopulationCount);

				networkedPopulation[layer].add(nodePopulation);
			}
		}
	}

	List<intersectionmanagement.simulator.track.Node> testTrack;

	public void setTrack(List<intersectionmanagement.simulator.track.Node> track)
	{
		this.testTrack = track;
	}

	public void run()
	{
		calculateFitnesses(testTrack);
		evolutionaryTick();
	}

	public void calculateFitnesses(List<intersectionmanagement.simulator.track.Node> track)
	{
		ForkJoinPool commonPool = ForkJoinPool.commonPool();
		ANNFitnessMultithread fitnessCalc = new ANNFitnessMultithread(networkedPopulation, getBestNetwork(), track);

		commonPool.invoke(fitnessCalc);

		setFitnessOfOldBest();
	}

	public int getSomeCrashValue()
	{
		return networkedPopulation[0].get(0)[0].getValue();
	}

	private void setFitnessOfOldBest()
	{
		int best = networkedPopulation[0].get(0)[0].getValue();
		int layerPosOfBest = 0;
		int positionInLayerOfBest = 0;
		Node[] pop;

		for (int layer=0; layer<NodeConfig.layerSizes.length-1; layer++)
			for (int nodePopulationCount=0; nodePopulationCount<NodeConfig.layerSizes[layer]; nodePopulationCount++)
			{
				pop = networkedPopulation[layer].get(nodePopulationCount);
				for (int i=1; i<pop.length; i++)
					if (pop[i].getValue() > best)
					{
						best = pop[i].getValue();
						layerPosOfBest = layer;
						positionInLayerOfBest = nodePopulationCount;
					}
			}

		for (int layer=0; layer<NodeConfig.layerSizes.length-1; layer++)
			for (int nodePopulationCount=0; nodePopulationCount<NodeConfig.layerSizes[layer]; nodePopulationCount++)
				if (layer != layerPosOfBest && nodePopulationCount != positionInLayerOfBest)
					networkedPopulation[layer].get(nodePopulationCount)[0].setValue(best);
	}

	private void setupLinearRankTable()
	{
		double n_plus = NodeConfig.selectionPressure;
		double n_minus = 2 - n_plus;
		int N = NodeConfig.populationSize;

		linearRankTable[0] = (n_minus + (2*n_plus-2))/N;
		for (int i=1; i<NodeConfig.populationSize; i++)
			linearRankTable[i] = linearRankTable[i-1] + (n_minus + ((2*n_plus-2)*(N-i-1))/(N-1))/N;
	}

	private void populationEvolutionaryTick(int layer, int populationInLayer)
	{
		Node population[] = networkedPopulation[layer].get(populationInLayer);
		
		Node newPopulation[] = new Node[NodeConfig.populationSize];
		newPopulation[0] = population[0];
		int p1, p2;
		
		for(int i=1; i<NodeConfig.populationSize; i+=2)
		{
			p1 = linearRankSelection();
			p2 = linearRankSelection();

			crossover(population, p1, p2, newPopulation, i);
			newPopulation[i].mutate();
			newPopulation[i+1].mutate();
		}
		networkedPopulation[layer].set(populationInLayer, newPopulation);
	}

	private int linearRankSelection()
	{
		double selected = NodeConfig.rng.nextDouble();
		for(int i=0; i<NodeConfig.populationSize; i++)
			if (selected < linearRankTable[i])
				return i;
		return -1;
	}

	public byte[] getBestNetworkBytes()
	{
		return SerializationUtils.serialize(getBestNetwork());
	}

	public BasicNetwork getBestNetwork()
	{
		BasicNetwork net = new BasicNetwork();
		net.addLayer(new BasicLayer(null,true,NodeConfig.layerSizes[0]));

		for(int layer=1; layer<NodeConfig.layerSizes.length; layer++)
			net.addLayer(new BasicLayer(new ActivationSigmoid(),false,NodeConfig.layerSizes[layer]));
		net.getStructure().finalizeStructure();

		for(int layer=1; layer<NodeConfig.layerSizes.length; layer++)
			for (int node=0; node<NodeConfig.layerSizes[layer-1]; node++)
			{
				Node nodeWithWeights = networkedPopulation[layer-1].get(node)[0];
				for (int weight=0; weight<NodeConfig.layerSizes[layer]; weight++)
					net.setWeight(layer-1, node, weight, nodeWithWeights.getWeight(weight));
			}
		
		return net;
	}

	private void crossover(Node[] oldPopulation, int index1, int index2,
							Node[] newPopulation, int count)
	{
		double p1[] = oldPopulation[index1].getWeights();
		double p2[] = oldPopulation[index2].getWeights();

		
		double c1[] = new double[p1.length];
		double c2[] = new double[p1.length];

		if (NodeConfig.rng.nextDouble() < NodeConfig.crossoverRate && p1.length > 1)
		{
			int crossoverPoint = NodeConfig.rng.nextInt(p1.length-1)+1;
			for (int i=0; i<crossoverPoint; i++)
			{
				c1[i] = p1[i];
				c2[i] = p2[i];
			}
			for (int i=crossoverPoint; i<p1.length; i++)
			{
				c1[i] = p2[i];
				c2[i] = p1[i];
			}
		}else{
			c1 = p1;
			c2 = p2;
		}

		int layer = oldPopulation[index1].getLayer();
		int positionInLayer = oldPopulation[index1].getPositionInLayer();

		newPopulation[count] = new Node(c1, layer, positionInLayer);
		newPopulation[count+1] = new Node(c2, layer, positionInLayer);
	}
}