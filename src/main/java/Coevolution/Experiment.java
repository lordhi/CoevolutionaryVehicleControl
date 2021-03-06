package coevolution;

import java.util.*;
import java.io.*;
import intersectionmanagement.trial.Trial;


public class Experiment
implements Runnable
{
	static byte[] controller;
	static List<intersectionmanagement.simulator.track.Node> track;

	public static void main(String args[])
	throws Exception
	{
		int max = Integer.parseInt(args[0]);
		readTrackAndController(args[1]);
		startAll(max);
	}

	public static void startAll(int max)
	{
		Thread threads[] = new Thread[max];
		for (int i=0; i<max; i++)
		{
			threads[i] = new Thread(new Experiment());
			threads[i].start();
		}
		for (int i=0; i<max; i++)
			try{
				threads[i].join();
			}catch(Exception e){
				e.printStackTrace();
			}
	}

	public Experiment()
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

	private static void readTrackAndController(String filename)
	throws Exception
	{
		String vals[] = readFromFile(filename);

		controller = StringToBytes(vals[0]);
		track = StringToTrack(vals[1]);
	}

	public static List<intersectionmanagement.simulator.track.Node> StringToTrack(String s)
	{
		List<intersectionmanagement.simulator.track.Node> track = new ArrayList<>();

		String nodes[] = s.split(";");
		for (String node : nodes)
		{
			track.add(stringToCurve(node).asSimCurve());
		}

		return track;
	}

	private static BezierCurve stringToCurve(String s)
	{
		String tmp[] = s.split(":");

		for (int i=0; i<tmp.length; i++)
			tmp[i] = tmp[i].substring(tmp[i].indexOf("[")+1, tmp[i].indexOf("]"));
			
		float x[] = stringToFloatArray(tmp[0]);
		float y[] = stringToFloatArray(tmp[1]);
		
		return new BezierCurve(x,y,0);
	}

	private static float[] stringToFloatArray(String s)
	{
		String ss[] = s.split(",");
		float tmp[] = new float[ss.length];

		for (int i=0; i<ss.length; i++)
			tmp[i] = Float.parseFloat(ss[i]);

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