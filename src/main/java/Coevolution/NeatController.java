package coevolution;

import java.util.List;

import org.apache.commons.lang3.SerializationUtils;

import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.neat.NEATNetwork;
import org.encog.ml.CalculateScore;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

public class NeatController
implements Runnable
{
	EvolutionaryAlgorithm trainer;

	int seed;
	List<intersectionmanagement.simulator.track.Node> testTrack;

	public NeatController()
	throws Exception
	{
		NEATPopulation pop = new NEATPopulation(8,1, 250);
		pop.setInitialConnectionDensity(1.0);
		pop.reset();

		CalculateScore score = new CrashScore(this);

		trainer = NEATUtil.constructNEATTrainer(pop, score);
	}

	public int getSomeCrashValue()
	{
		return (int) trainer.getBestGenome().getAdjustedScore();
	}

	public byte[] getBestNetwork()
	{
		return SerializationUtils.serialize((NEATNetwork)trainer.getCODEC().decode(trainer.getBestGenome()));
	}

	public void setTrack(List<intersectionmanagement.simulator.track.Node> track)
	{
		this.testTrack = track;
	}

	public List<intersectionmanagement.simulator.track.Node> getTrack()
	{
		return testTrack;
	}

	public int getSeed()
	{
		return seed;
	}

	public void run()
	{
		seed = NodeConfig.rng.nextInt();
		trainer.iteration();
	}
}