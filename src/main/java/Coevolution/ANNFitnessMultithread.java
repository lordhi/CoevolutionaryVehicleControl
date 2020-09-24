package coevolution;

import java.util.concurrent.RecursiveAction;
import java.util.List;
import java.util.ArrayList;

import org.encog.neural.networks.BasicNetwork;
import intersectionmanagement.trial.Trial;

import org.apache.commons.lang3.SerializationUtils;


class ANNFitnessMultithread
extends RecursiveAction
{
	final static int THRESHOLD = 10;

	static List<intersectionmanagement.simulator.track.Node> track;
	static BasicNetwork best;
	static int seed;

	static Node nodes[];
	static Node base[];

	final int lo, hi;

	public ANNFitnessMultithread(ArrayList<Node[]> networkedPopulation[],
		BasicNetwork bestNetwork, List<intersectionmanagement.simulator.track.Node> track)
	{
		seed = NodeConfig.rng.nextInt();
		this.best = bestNetwork;
		this.track = track;

		int nodeCount = NodeConfig.totalNodePopulations*(NodeConfig.populationSize-1)+1;
		nodes = new Node[nodeCount];
		base = new Node[NodeConfig.totalNodePopulations];

		lo = 0;
		hi = nodeCount;

		nodes[0] = networkedPopulation[0].get(0)[0];

		int i=1;
		int j=0;
		Node currentPop[];

		for (int layer = 0; layer < NodeConfig.layerSizes.length-1; layer++)
		{
			for (int populationInLayer=0; populationInLayer<NodeConfig.layerSizes[layer]; populationInLayer++)
			{
				currentPop = networkedPopulation[layer].get(populationInLayer);
				base[j++] = currentPop[0];
				
				for (int nodeInPopulation=1; nodeInPopulation<NodeConfig.populationSize; nodeInPopulation++)
					nodes[i++] = currentPop[nodeInPopulation];	
			}
		}
	}

	public ANNFitnessMultithread(int lo, int hi)
	{
		this.lo = lo;
		this.hi = hi;
	}

	@Override
	protected void compute()
	{
		if (hi - lo < THRESHOLD)
			sequentialCalc(lo, hi);
		else {
			int mid = (lo + hi) >>> 1;
			invokeAll(new ANNFitnessMultithread(lo, mid),
				 new ANNFitnessMultithread(mid, hi));
		}
	}

	private void sequentialCalc(int lo, int hi)
	{
		for (int i=lo; i<hi; i++)
		{
			try{
				calculateFitnessForNode(nodes[i]);
			}catch(Exception e){
				e.printStackTrace();
				System.exit(0);
			}
		}

		if (lo==0)
		{
			int crashes = nodes[0].getValue();
			for (int population=0; population<base.length; population++)
				base[population].setValue(crashes);
		}
	}

	private void calculateFitnessForNode(Node node)
	throws Exception
	{
		int layer = node.getLayer();
		int populationInLayer = node.getPositionInLayer();

		BasicNetwork net = (BasicNetwork) best.clone();
		for (int weight=0; weight<NodeConfig.layerSizes[layer+1]; weight++)
			net.setWeight(layer, populationInLayer, weight, node.getWeight(weight));

		Trial trial = new Trial(seed, track, SerializationUtils.serialize(net));
		int crashes = trial.runSimulation();

		node.setValue(crashes);
	}

}