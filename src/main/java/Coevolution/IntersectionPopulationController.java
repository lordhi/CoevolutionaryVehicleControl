package coevolution;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

import intersectionmanagement.trial.Trial;


public class IntersectionPopulationController
implements Runnable
{
	ArrayList<BezierCurve[]> curvePopulations;
	double[] linearRankTable = new double[IntersectionConfig.populationSize];
	boolean generateRandomly = true;


	public IntersectionPopulationController(int numberOfEntries, int lanesPerEntry)
	{
		this(numberOfEntries, lanesPerEntry, false);
	}

	public IntersectionPopulationController(boolean generateRandomly, int numberOfEntries, int lanesPerEntry)
	{
		this(numberOfEntries, lanesPerEntry, false);
		this.generateRandomly = generateRandomly;
	}

	public IntersectionPopulationController(int numberOfEntries, int lanesPerEntry,
		boolean allowUTurn)
	{
		float xCoords[][][] = new float[2][numberOfEntries][lanesPerEntry];
		float yCoords[][][] = new float[2][numberOfEntries][lanesPerEntry];
		float minmaxXY[][] = setupEntryAndExitCoordinates(xCoords, yCoords, numberOfEntries, lanesPerEntry);

		curvePopulations = new ArrayList<>(calculateNumberOfCurves(numberOfEntries, lanesPerEntry, allowUTurn));
		initialiseCurvePopulations(curvePopulations, allowUTurn, numberOfEntries, lanesPerEntry, minmaxXY, xCoords, yCoords);
		setupLinearRankTable();
	}

	public int getSomeCrashValue()
	{
		return curvePopulations.get(0)[0].getValue();
	}

	public ArrayList<intersectionmanagement.simulator.track.Node> getBestIntersection()
	{
		ArrayList<intersectionmanagement.simulator.track.Node> bestIntersection = new ArrayList<>(curvePopulations.size());

		for(int i=0; i<curvePopulations.size(); i++)
			bestIntersection.add(curvePopulations.get(i)[0].asSimCurve());

		return bestIntersection;
	}

	public BezierCurve[] getBestIntersectionPrintable()
	{
		BezierCurve best[] = new BezierCurve[curvePopulations.size()];
		for(int i=0; i<curvePopulations.size(); i++)
			best[i] = curvePopulations.get(i)[0];

		return best;
	}

	byte testController[];

	public void setController(byte[] controller)
	{
		this.testController = controller;
	}

	public void run()
	{
		calculateFitnesses(testController);
		evolutionaryTick();
	}

	public void calculateFitnesses(byte[] controller)
	{
		ForkJoinPool commonPool = ForkJoinPool.commonPool();
		IntersectionFitnessMultithread fitnessCalc = new 
			IntersectionFitnessMultithread(curvePopulations, controller, this);

		commonPool.invoke(fitnessCalc);

		setFitnessOfOldBest();
	}

	private void setFitnessOfOldBest()
	{
		int best = curvePopulations.get(0)[0].getValue();
		int populationOfBest = 0;

		BezierCurve currentPop[];

		for(int population=0; population < curvePopulations.size(); population++)
		{
			currentPop = curvePopulations.get(population);
			for(int i=1; i<curvePopulations.get(population).length; i++)
			{
				if (best < currentPop[i].getValue())
				{
					best = currentPop[i].getValue();
					populationOfBest = population;
				}
			}
		}

		for(int population=0; population < curvePopulations.size(); population++)
			if (population != populationOfBest)
				curvePopulations.get(population)[0].setValue(best);

	}

	public void evolutionaryTick()
	{
		for (int population=0; population<curvePopulations.size(); population++)
		{
			Arrays.sort(curvePopulations.get(population));
			populationEvolutionaryTick(population);
		}
	}

	private void populationEvolutionaryTick(int pop)
	{
		BezierCurve population[] = curvePopulations.get(pop);

		BezierCurve newPopulation[] = new BezierCurve[IntersectionConfig.populationSize];
		newPopulation[0] = population[0];
		int p1, p2;
		
		for(int i=1; i<IntersectionConfig.populationSize; i+=2)
		{
			p1 = linearRankSelection();
			p2 = linearRankSelection();

			crossover(population, p1, p2, newPopulation, i);
			newPopulation[i].mutate();
			newPopulation[i+1].mutate();
		}
		curvePopulations.set(pop, newPopulation);
	}

	private void initialiseCurvePopulations(ArrayList<BezierCurve[]> curvePopulations,
		boolean allowUTurn, int numberOfEntries, int lanesPerEntry, float minMaxXY[][],
		float xCoords[][][], float yCoords[][][])
	{
		for(int entryside=0; entryside<numberOfEntries; entryside++)
		{
			for(int exitside=0; exitside<numberOfEntries; exitside++)
			{
				if (entryside != exitside)
				{
					for(int lane=0; lane<lanesPerEntry; lane++)
					{
						BezierCurve population[] = new BezierCurve[IntersectionConfig.populationSize];

						for(int i=0; i<IntersectionConfig.populationSize; i++)
						{
							if (generateRandomly)
							{
								population[i] = new BezierCurve(
								xCoords[0][entryside][lane], yCoords[0][entryside][lane],
								xCoords[1][exitside][lane], yCoords[1][exitside][lane],
								IntersectionConfig.degree, curvePopulations.size());
							}else{
								float x1 = xCoords[0][entryside][lane];
								float x2 = xCoords[1][exitside][lane];
								float y1 = yCoords[0][entryside][lane];
								float y2 = yCoords[1][exitside][lane];

								float newX[] = {x1, 200.0f, x2};
								float newY[] = {y1, 200.0f, y2};

								population[i] = new BezierCurve(newX, newY, curvePopulations.size());
							}
						}
						curvePopulations.add(population);
					}
				}
			}
		}
	}

	private int calculateNumberOfCurves(int numberOfEntries, int lanesPerEntry, boolean allowUTurn)
	{
		int numberOfCurves;
		if (allowUTurn)
			numberOfCurves = numberOfEntries*numberOfEntries;
		else
			numberOfCurves = numberOfEntries*(numberOfEntries-1);
		return numberOfCurves*lanesPerEntry;
	}

	private float[][] setupEntryAndExitCoordinates(float xCoords[][][], float yCoords[][][],
		int numberOfEntries, int lanesPerEntry)
	{
		float minx = 0.0f;
		float miny = 0.0f;
		float maxx = 0.0f;
		float maxy = 0.0f;

		for (int lane=0; lane<lanesPerEntry; lane++)
		{
			xCoords[0][0][lane] = (float) (-0.5*IntersectionConfig.intersectionWidth);
			yCoords[0][0][lane] = (float) (0.5*(1+lane)*IntersectionConfig.laneWidth);

			xCoords[1][0][lane] = (float) (-0.5*IntersectionConfig.intersectionWidth);
			yCoords[1][0][lane] = (float) (-0.5*(1+lane)*IntersectionConfig.laneWidth);
		}
		
		// Rotates the first point around the axis to create new entries and exits on each side
		double angleBetweenSides = 2*Math.PI/numberOfEntries;
		for (int entryOrExit=0; entryOrExit<2; entryOrExit++)
		{
			for(int side=1; side<numberOfEntries; side++)
			{
				double angle = angleBetweenSides*side;
				for(int lane=0; lane<lanesPerEntry; lane++)
				{
					xCoords[entryOrExit][side][lane] = (float) (Math.cos(angle)*xCoords[entryOrExit][0][lane] - Math.sin(angle)*yCoords[entryOrExit][0][lane]);
					yCoords[entryOrExit][side][lane] = (float) (Math.sin(angle)*xCoords[entryOrExit][0][lane] + Math.cos(angle)*yCoords[entryOrExit][0][lane]);
				
					if (minx < xCoords[entryOrExit][side][lane])
						minx = xCoords[entryOrExit][side][lane];
					if (miny < yCoords[entryOrExit][side][lane])
						miny = yCoords[entryOrExit][side][lane];
				}
			}
		}

		// Ensures that all coordinates are positive. Adding a bit to get them into a similar range as examples
		minx += 150.0;
		miny += 150.0;
		for (int entryOrExit=0; entryOrExit<2; entryOrExit++)
			for(int side=0; side<numberOfEntries; side++)
				for(int lane=0; lane<lanesPerEntry; lane++)
				{
					xCoords[entryOrExit][side][lane] += minx;
					yCoords[entryOrExit][side][lane] += miny;

					if (xCoords[entryOrExit][side][lane] > maxx)
						maxx = xCoords[entryOrExit][side][lane];
					if (yCoords[entryOrExit][side][lane] > maxy)
						maxy = yCoords[entryOrExit][side][lane];
				}

		float minMaxXY[][] = new float[2][2];
		minMaxXY[0][0] = minx;
		minMaxXY[0][1] = maxx;
		minMaxXY[1][0] = miny;
		minMaxXY[1][1] = maxy;

		return minMaxXY;
	}

	private void crossover(BezierCurve[] oldPopulation, int index1, int index2,
							BezierCurve[] newPopulation, int count)
	{
		float xp1[] = oldPopulation[index1].getXPositions();
		float xp2[] = oldPopulation[index2].getXPositions();

		float yp1[] = oldPopulation[index1].getYPositions();
		float yp2[] = oldPopulation[index2].getYPositions();
		

		float nxp1[] = new float[xp1.length];
		float nxp2[] = new float[xp2.length];
		float nyp1[] = new float[yp1.length];
		float nyp2[] = new float[yp2.length];

		if (NodeConfig.rng.nextDouble() < NodeConfig.crossoverRate)
		{
			nxp1[0] = xp1[0];
			nyp1[0] = yp1[0];

			nxp2[0] = xp2[0];
			nyp2[0] = yp2[0];
			for (int i=1; i<xp1.length; i++)
			{
				nxp1[i] = (2*xp1[i] +xp2[i])/3;
				nxp2[i] = (2*xp2[i] +xp1[i])/3;

				nyp1[i] = (2*yp1[i] +yp2[i])/3;
				nyp2[i] = (2*yp2[i] +yp1[i])/3;
			}
		}else{
				nxp1 = xp1;
				nxp2 = xp2;

				nyp1 = yp1;
				nyp2 = yp2;
		}

		newPopulation[count] = new BezierCurve(nxp1, nyp1, oldPopulation[index1].getPopulation());
		newPopulation[count+1] = new BezierCurve(nxp2, nyp2, oldPopulation[index2].getPopulation());
	}

	private int linearRankSelection()
	{
		double selected = NodeConfig.rng.nextDouble();
		for(int i=0; i<IntersectionConfig.populationSize; i++)
			if (selected < linearRankTable[i])
				return i;
		return -1;
	}

	private void setupLinearRankTable()
	{
		double n_plus = IntersectionConfig.selectionPressure;
		double n_minus = 2 - n_plus;
		int N = IntersectionConfig.populationSize;

		linearRankTable[0] = (n_minus + (2*n_plus-2))/N;
		for (int i=1; i<IntersectionConfig.populationSize; i++)
			linearRankTable[i] = linearRankTable[i-1] + (n_minus + ((2*n_plus-2)*(N-i-1))/(N-1))/N;
	}
}