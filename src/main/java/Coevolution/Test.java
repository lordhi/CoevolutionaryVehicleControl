package coevolution;

import java.util.Arrays;

import org.encog.neural.networks.BasicNetwork;
import intersectionmanagement.trial.Trial;
import org.apache.commons.lang3.SerializationUtils;

public class Test
{
	public static void main(String args[])
	throws Exception
	{
		testNeatOnly();
	}

	public static void testStandard()
	throws Exception
	{
		IntersectionPopulationController ipc = new IntersectionPopulationController(8,1);

		NodePopulationController npc = new NodePopulationController();
		long start;
		int best = 2147483647;
		for (int i=1; i<500; i++)
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
				System.err.println(Arrays.toString(npc.getBestNetworkBytes()));
				System.err.println(bezierCurveArrayToString(ipc.getBestIntersectionPrintable()));
				System.err.println();
			}

			System.out.println(i + ": " + npc.getSomeCrashValue() + " | " + best +  " (" + (System.currentTimeMillis()-start)/1000 + " s)");
		}
		
	}

	private static String bezierCurveArrayToString(BezierCurve curves[])
	{
		String tmp = "";

		for(BezierCurve curve : curves)
			tmp += curve.toString() + ";";

		return tmp.substring(0, tmp.length()-1);
	}

	public static void testNEAT()
	throws Exception
	{
		IntersectionPopulationController ipc = new IntersectionPopulationController(8,1);

		NeatController nc = new NeatController();
		long start;
		int best = 2147483647;
		for (int i=1; i<2000; i++)
		{
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
				System.err.println(Arrays.toString(nc.getBestNetwork()));
				System.err.println(Arrays.toString(ipc.getBestIntersectionPrintable()));
				System.err.println();
			}

			System.out.println(i + ": " + nc.getSomeCrashValue() + " | " + best +  " (" + (System.currentTimeMillis()-start)/1000 + " s)");
		}
		
	}
		

	public static void testIntersections()
	throws Exception
	{
		NodePopulationController npc = new NodePopulationController();
		byte[] controller = npc.getBestNetworkBytes();

		IntersectionPopulationController ipc = new IntersectionPopulationController(4,1);

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

	public static void testNeatOnly()
	throws Exception
	{
		IntersectionPopulationController ipc = new IntersectionPopulationController(4,1);

		NeatController nc = new NeatController();
		long start;

		for (int i=1; i<1000; i++)
		{
			start = System.currentTimeMillis();
			nc.setTrack(ipc.getBestIntersection());
			nc.run();

			System.err.println(Arrays.toString(nc.getBestNetwork()));
			System.out.println(i + ": " + nc.getSomeCrashValue() + " (" + (System.currentTimeMillis()-start)/1000 + " s)");
		}
	}
}