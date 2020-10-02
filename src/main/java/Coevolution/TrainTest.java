package coevolution;

public class TrainTest
{
	public static void main(String args[])
	{
		trainTestNeatOnly();
	}

	private static void trainTestCoSyNE()
	{
		try{
			SimulatorConfig.simulationSteps = 4000;
			String fourway[] = Training.testStandard(4);
			String eightway[] = Training.testStandard(8);

			test(fourway, eightway);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void trainTestNeat()
	{
		try{
			SimulatorConfig.simulationSteps = 4000;
			String fourway[] = Training.testNEAT(4);
			String eightway[] = Training.testNEAT(8);

			test(fourway, eightway);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void test(String fourway[], String eightway[])
	{
		byte fourwayController[] = Experiment1.StringToBytes(fourway[0]);
		byte eightwayController[] = Experiment1.StringToBytes(eightway[0]);

		SimulatorConfig.simulationSteps = 100000;
		//Tests individual tracks
		Experiment xp = new Experiment();
		xp.track = Experiment.StringToTrack(fourway[1]);
		xp.controller = fourwayController;
		xp.startAll(100);
		System.out.println("\r\nFourway finished.\r\n");

		xp = new Experiment();
		xp.track = Experiment.StringToTrack(eightway[1]);
		xp.controller = eightwayController;
		xp.startAll(100);
		System.out.println("\r\nEightway finished.\r\n");


		//Tests combined track
		Experiment1 xp1 = new Experiment1();
		xp1.StringToTrack(eightway[1], eightwayController, 0);
		xp1.StringToTrack(fourway[1], fourwayController, 1);
		xp1.StringToTrack(fourway[1], fourwayController, 2);
		xp1.StringToTrack(fourway[1], fourwayController, 3);

		xp1.startAll(200);
	}

	private static void trainTestNeatOnly()
	{
		try{
			SimulatorConfig.simulationSteps = 4000;
			byte[] neatController = Training.testNeatOnly();

			SimulatorConfig.simulationSteps = 100000;
			Experiment1 xp1 = new Experiment1();

			//Tests combined track
			String tracks[] = Training.generateBasic();
			xp1.StringToTrack(tracks[1], neatController, 0);
			xp1.StringToTrack(tracks[0], neatController, 1);
			xp1.StringToTrack(tracks[0], neatController, 2);
			xp1.StringToTrack(tracks[0], neatController, 3);

			xp1.startAll(200);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}