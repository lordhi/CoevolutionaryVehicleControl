package coevolution;

import java.util.Arrays;

import intersectionmanagement.simulator.track.TrackParser;
import intersectionmanagement.simulator.control.NeuralNetworkController;

public class BezierCurve
implements Comparable<BezierCurve>
{
	private int degree;
	private int precision;
	private boolean active;

	private float xPositions[];
	private float yPositions[];

	private int population;

	private int value;

	public BezierCurve(float startX, float startY, float endX, float endY,
		int degree, int population)
	{
		this(generateArray(startX, endX, degree), generateArray(startY, endY, degree), population);
	}

	public BezierCurve(float startX, float startY, float endX, float endY,
		int degree, int precision, boolean active, int population)
	{
		this(generateArray(startX, endX, degree), generateArray(startY, endY, degree), precision, active, population);
	}

	public BezierCurve(float xPositions[], float yPositions[],
		int precision, boolean active, int population)
	{
		this.value = 2147483647;
		this.population = population;
		this.xPositions = new float[xPositions.length];
		this.yPositions = new float[yPositions.length];

		for (int i=0; i<xPositions.length; i++)
		{
			this.xPositions[i] = xPositions[i];
			this.yPositions[i] = yPositions[i];
		}

		degree = xPositions.length;
		this.precision = precision;
		this.active = active;
	}

	public BezierCurve(float xPositions[], float yPositions[], int population)
	{
		this(xPositions, yPositions, 10, true, population);
	}

	public intersectionmanagement.simulator.track.Node asSimCurve(byte controller[])
	{
		return TrackParser.calculateBezierCurve(degree,
			precision, xPositions, yPositions, active, new NeuralNetworkController(controller));
	}

	public intersectionmanagement.simulator.track.Node asSimCurve()
	{
		return TrackParser.calculateBezierCurve(degree,
			precision, xPositions, yPositions, active, null);
	}

	private static float[] generateArray(float firstVal, float lastVal, int length)
	{
		float arr[] = new float[length];
		arr[0] = firstVal;
		arr[length-1] = lastVal;

		for (int i=1; i<length-1; i++)
		{
			arr[i] = generateFloatInRange(firstVal, lastVal);	
		}

		return arr;
	}

	private static float generateFloatInRange(float firstVal, float lastVal)
	{
		float lower = firstVal < lastVal ? firstVal : lastVal;
		float higher = firstVal > lastVal ? firstVal : lastVal;

		lower -= 10;
		higher += 10;

		return lower + (float) (NodeConfig.rng.nextDouble()*(higher-lower));
	}

	public int getValue()
	{
		return this.value;
	}

	public int getPopulation()
	{
		return this.population;
	}

	public void setValue(int val)
	{
		this.value = val;
	}

	public int compareTo(BezierCurve bc)
	{
		return Double.compare(value, bc.getValue());
	}

	public void mutate()
	{
		for (int i=1; i<xPositions.length-1; i++)
			if (NodeConfig.rng.nextDouble() < IntersectionConfig.mutationRate)
				xPositions[i] += 2*NodeConfig.rng.nextDouble()*IntersectionConfig.mutationDelta - IntersectionConfig.mutationDelta;
		
		for (int i=1; i<yPositions.length-1; i++)
			if (NodeConfig.rng.nextDouble() < IntersectionConfig.mutationRate)
				yPositions[i] += 2*NodeConfig.rng.nextDouble()*IntersectionConfig.mutationDelta - IntersectionConfig.mutationDelta;
	}

	public float[] getXPositions()
	{
		float arr[] = new float[xPositions.length];
		for (int i=0; i<xPositions.length; i++)
			arr[i] = xPositions[i];
		return arr;
	}

	public float[] getYPositions()
	{
		float arr[] = new float[yPositions.length];
		for (int i=0; i<yPositions.length; i++)
			arr[i] = yPositions[i];
		return arr;
	}

	@Override
	public String toString()
	{
		return Arrays.toString(xPositions) + " : " + Arrays.toString(yPositions);
	}
}