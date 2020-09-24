package coevolution;

import java.util.concurrent.RecursiveAction;
import java.util.List;
import java.util.ArrayList;

import org.encog.neural.networks.BasicNetwork;
import intersectionmanagement.trial.Trial;

import org.apache.commons.lang3.SerializationUtils;


class IntersectionFitnessMultithread
extends RecursiveAction
{
	final static int THRESHOLD = 10;

	static byte[] controller;
	static IntersectionPopulationController ipc;
	static int seed;

	static BezierCurve[] nodes;
	static ArrayList<BezierCurve[]> curvePopulations;

	final int lo, hi;

	public IntersectionFitnessMultithread(ArrayList<BezierCurve[]> curvePopulations,
		byte[] controller, IntersectionPopulationController ipc)
	{
		seed = NodeConfig.rng.nextInt();
		this.controller = controller;
		this.ipc = ipc;
		this.curvePopulations = curvePopulations;

		int nodeCount = curvePopulations.size()*(IntersectionConfig.populationSize-1)+1;
		nodes = new BezierCurve[nodeCount];

		lo = 0;
		hi = nodeCount;

		nodes[0] = curvePopulations.get(0)[0];

		int i=1;
		int j=0;
		BezierCurve currentPop[];

		for (int population=0; population<curvePopulations.size(); population++)
		{
			currentPop = curvePopulations.get(population);
			
			for (int nodeInPopulation=1; nodeInPopulation<IntersectionConfig.populationSize; nodeInPopulation++)
				nodes[i++] = currentPop[nodeInPopulation];	
		}
	}

	public IntersectionFitnessMultithread(int lo, int hi)
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
			invokeAll(new IntersectionFitnessMultithread(lo, mid),
				 new IntersectionFitnessMultithread(mid, hi));
		}
	}

	private void sequentialCalc(int lo, int hi)
	{
		for (int i=lo; i<hi; i++)
		{
			try{
				calculateFitnessForCurve(nodes[i]);
			}catch(Exception e){
				e.printStackTrace();
				System.exit(0);
			}
		}

		if (lo==0)
		{
			int crashes = nodes[0].getValue();
			for (int population=0; population<curvePopulations.size(); population++)
				curvePopulations.get(population)[0].setValue(crashes);
		}
	}

	public int calculateFitnessForCurve(BezierCurve curve)
	throws Exception
	{
		List<intersectionmanagement.simulator.track.Node> currentTrack = ipc.getBestIntersection();
		currentTrack.set(curve.getPopulation(), curve.asSimCurve());

		Trial trial = new Trial(seed, currentTrack, controller);
		int crashes = trial.runSimulation();

		curve.setValue(crashes);

		return crashes;
	}
}