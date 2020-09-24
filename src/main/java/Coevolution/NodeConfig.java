package coevolution;

public class NodeConfig
{
	static final int populationSize = 50+1;
	static final MersenneTwister rng = new MersenneTwister();

	static final int layerSizes[] = {8,7,4,1};
	static final int totalNodePopulations = 8+7+4;

	// Mutation rate is relative to 1/L, with L being the number of weights in a node
	static final double mutationRate = 1.0;
	static final double crossoverRate = 0.6;

	static double selectionPressure = 1.5;
	static double mutationDelta = 0.2;
}