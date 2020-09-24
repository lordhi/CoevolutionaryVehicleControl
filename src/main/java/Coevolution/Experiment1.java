package coevolution;

import java.util.*;
import java.io.*;
import intersectionmanagement.trial.Trial;
import intersectionmanagement.simulator.track.TrackParser;

// In this experiment, each intersection is controlled by a different controller

public class Experiment1
implements Runnable
{
	static byte[] controller;
	static List<intersectionmanagement.simulator.track.Node> track = new ArrayList<>();

	static float xOffsets[] = {-55, 55, -55, 55};
	static float yOffsets[] = {-55, -55, 55, 55};
	static float manualX[][] = {{250,250}, {260,260},
									{195,205}, {205,195},
									{140,140}, {150,150},
									{205,195}, {195,205}};
	static float manualY[][] = {{195,205}, {205,195},
									{260,260}, {250,250},
									{195,205}, {205,195},
									{140,140}, {150,150}};

	public static void main(String args[])
	throws Exception
	{
		int max = Integer.parseInt(args[0]);
		readTrackAndController("18" + args[1] + ".txt", 0);
		readTrackAndController("14" + args[1] + ".txt", 1);
		readTrackAndController("14" + args[1] + ".txt", 2);
		readTrackAndController("14" + args[1] + ".txt", 3);
		addManualCurves();

		track = TrackParser.linkCurves(track);

		for (int i=0; i<max; i++)
		{
			Thread t = new Thread(new Experiment1());
			t.run();
		}
	}

	public Experiment1()
	{

	}

	public void run()
	{
		MersenneTwister rng = new MersenneTwister();
		try{
			Trial trial = new Trial(rng.nextInt(), track, controller);
			System.out.println(trial.runSimulation());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void readTrackAndController(String filename, int index)
	throws Exception
	{
		String vals[] = readFromFile(filename);

		controller = StringToBytes(vals[0]);
		StringToTrack(vals[1], controller, index);
	}

	private static void addManualCurves()
	{
		//float xPositions[], float yPositions[]
		//int precision, boolean active, int population
		for (int i=0; i<manualX.length; i++)
			track.add((new BezierCurve(manualX[i], manualY[i],
				2, true, 0)).asSimCurve(controller));
	}

	private static void StringToTrack(String s, byte controller[], int index)
	{
		String nodes[] = s.split(";");
		for (String node : nodes)
		{
			track.add(stringToCurve(node, index).asSimCurve(controller));
		}
	}

	private static BezierCurve stringToCurve(String s, int index)
	{
		String tmp[] = s.split(":");

		for (int i=0; i<tmp.length; i++)
			tmp[i] = tmp[i].substring(tmp[i].indexOf("[")+1, tmp[i].indexOf("]"));
			
		float x[] = stringToFloatArray(tmp[0], xOffsets[index]);
		float y[] = stringToFloatArray(tmp[1], yOffsets[index]);
		
		return new BezierCurve(x,y,0);
	}

	private static float[] stringToFloatArray(String s, float offset)
	{
		String ss[] = s.split(",");
		float tmp[] = new float[ss.length];

		for (int i=0; i<ss.length; i++)
			tmp[i] = Float.parseFloat(ss[i]) + offset;

		return tmp;
	}

	private static byte[] StringToBytes(String s)
	{
		String bytes[] = s.substring(1,s.length()-1).replace(" ", "").split(",");

		byte controller[] = new byte[bytes.length];
		for (int i=0; i<bytes.length; i++)
			controller[i] = Byte.parseByte(bytes[i]);

		return controller;
	}

	private static String[] readFromFile(String filename)
	throws Exception
	{
		BufferedReader in = new BufferedReader(new FileReader(new File(filename)));
		String all[] = new String[2];

		all[0] = in.readLine();
		all[1] = in.readLine();

		return all;
	}
}