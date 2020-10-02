package coevolution;

import java.util.Arrays;

import org.encog.neural.networks.BasicNetwork;
import intersectionmanagement.trial.Trial;
import org.apache.commons.lang3.SerializationUtils;

public class Training
{
	public static void main(String args[])
	throws Exception
	{
		generateBasic();
	}

	public static String[] testStandard(int directions)
	throws Exception
	{
		int max = directions==4 ? 350 : 80;
		IntersectionPopulationController ipc = new IntersectionPopulationController(directions,1);

		NodePopulationController npc = new NodePopulationController();
		long start;
		int best = 2147483647;

		String[] bestResults = new String[2];

		for (int i=1; i<max; i++)
		{
			start = System.currentTimeMillis();
			npc.setTrack(ipc.getBestIntersection());
			ipc.setController(npc.getBestNetworkBytes());

			Thread ann = new Thread(npc);
			Thread net = new Thread(ipc);

			ann.start();
			net.start();

			ann.join();
			net.join();

			if (best > npc.getSomeCrashValue())
			{
				best = npc.getSomeCrashValue();
				bestResults[0] = Arrays.toString(npc.getBestNetworkBytes());
				bestResults[1] = bezierCurveArrayToString(ipc.getBestIntersectionPrintable());
			}
		}

		return bestResults;		
	}

	private static String bezierCurveArrayToString(BezierCurve curves[])
	{
		String tmp = "";

		for(BezierCurve curve : curves)
			tmp += curve.toString() + ";";

		return tmp.substring(0, tmp.length()-1);
	}

	public static String[] testNEAT(int directions)
	throws Exception
	{
		int max = directions==4 ? 600 : 80;

		IntersectionPopulationController ipc;

		NeatController nc = new NeatController();
		long start;
		int best = 2147483647;
		String[] bestResults = new String[2];


		for (int i=1; i<max; i++)
		{
			ipc = new IntersectionPopulationController(directions,1);
			start = System.currentTimeMillis();
			nc.setTrack(ipc.getBestIntersection());
			ipc.setController(nc.getBestNetwork());

			Thread ann = new Thread(nc);
			Thread net = new Thread(ipc);

			ann.start();
			net.start();

			ann.join();
			net.join();
	
			if (best > nc.getSomeCrashValue())
			{
				best = nc.getSomeCrashValue();
				bestResults[0] = Arrays.toString(nc.getBestNetwork());
				bestResults[1] = bezierCurveArrayToString(ipc.getBestIntersectionPrintable());
			}
		}
		return bestResults;
	}
		

	public static void testIntersections()
	throws Exception
	{
		NodePopulationController npc = new NodePopulationController();
		byte[] controller = npc.getBestNetworkBytes();

		IntersectionPopulationController ipc = new IntersectionPopulationController(false, 4,1);

		long start;
		for (int i=1; i<1000; i++)
		{
			start = System.currentTimeMillis();
			ipc.calculateFitnesses(controller);

			System.out.println(i + ": " + ipc.getSomeCrashValue()  + " (" + (System.currentTimeMillis()-start)/1000 + " s)");
			ipc.evolutionaryTick();
		} 
	}

	public static void testNodes()
	throws Exception
	{
		IntersectionPopulationController ipc = new IntersectionPopulationController(4,1);

		NodePopulationController npc = new NodePopulationController();
		long start;
		for (int i=1; i<1000; i++)
		{
			start = System.currentTimeMillis();
			npc.calculateFitnesses(ipc.getBestIntersection());

			System.out.println(i + ": " + npc.getSomeCrashValue() + " (" + (System.currentTimeMillis()-start)/1000 + " s)");
			npc.evolutionaryTick();
		}
	}

	public static byte[] testNeatOnly()
	throws Exception
	{
		IntersectionPopulationController ipc;

		NeatController nc = new NeatController();
		long start;

		int best = 2147483647;
		byte bestnet[] = null;

		for (int i=1; i<1000; i++)
		{
			ipc = new IntersectionPopulationController(NodeConfig.rng.nextInt(6)+4,NodeConfig.rng.nextInt(3)+1);
			start = System.currentTimeMillis();
			nc.setTrack(ipc.getBestIntersection());
			nc.run();

			if (best > nc.getSomeCrashValue())
			{
				best = nc.getSomeCrashValue();
				bestnet = nc.getBestNetwork();
			}
		}

		return bestnet;
	}

	public static String[] generateBasic()
	{
		String tracks[] = new String[2];
		IntersectionPopulationController ipc = new IntersectionPopulationController(false, 4,1);
		tracks[0] = bezierCurveArrayToString(ipc.getBestIntersectionPrintable());

		ipc = new IntersectionPopulationController(false, 8,1);
		tracks[1] = bezierCurveArrayToString(ipc.getBestIntersectionPrintable());

		return tracks;
	}
}