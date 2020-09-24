package coevolution;

import java.util.Arrays;

public class Node
implements Comparable<Node>
{
	private final double[] weights;
	private int value;

	private int layer;
	private int positionInLayer;

	public Node(int size, int layer, int positionInLayer)
	{
		this.layer = layer;
		this.positionInLayer = positionInLayer;
		this.value = 2147483647;

		weights = new double[size];
		//Max int value
		value = 2147483647;

		for(int i=0; i<size; i++)
			weights[i] = NodeConfig.rng.nextDouble()*2 - 1;
	}

	public Node(double[] weights, int layer, int positionInLayer)
	{
		this.value = 2147483647;
		this.layer = layer;
		this.positionInLayer = positionInLayer;

		this.weights = new double[weights.length];
		for (int i=0; i<weights.length; i++)
			this.weights[i] = weights[i];
	}

	public int compareTo(Node n)
	{
		return Double.compare(value, n.getValue());
	}

	public int getValue()
	{
		return this.value;
	}

	public int getLayer()
	{
		return this.layer;
	}

	public int getPositionInLayer()
	{
		return this.positionInLayer;
	}

	public void setValue(int val)
	{
		this.value = val;
	}

	public void mutate()
	{
		for (int i=0; i<weights.length; i++)
		{
			if (NodeConfig.rng.nextDouble() < NodeConfig.mutationRate/weights.length)
				weights[i] += 2*NodeConfig.rng.nextDouble()*NodeConfig.mutationDelta - NodeConfig.mutationDelta;
		}
	}

	public String toString()
	{
		return value + "";
	}

	public double getWeight(int pos)
	{
		return weights[pos];
	}

	public double[] getWeights()
	{
		double vals[] = new double[weights.length];
		for (int i=0; i<weights.length; i++)
			vals[i] = weights[i];
		return vals;
	}
}