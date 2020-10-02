package coevolution;

public class SimulatorConfig
{
	public static int simulationSteps = 4000;//100000;

	public final static String spawnerType = "constant";
	public final static int minPeriod = 200;
	public final static int maxPeriod = 200;
	public final static double periodMul = 1.0;

	public final static double randomness = 0.3;
	public final static float pedestrianRandomness = 4f;
	public final static int pedestrianRate = 1000;
}