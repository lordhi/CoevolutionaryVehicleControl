package intersectionmanagement.trial;

import intersectionmanagement.simulator.Actor;
import intersectionmanagement.simulator.Simulator;
import intersectionmanagement.simulator.car.Car;
import intersectionmanagement.simulator.pedestrian.Pedestrian;
import intersectionmanagement.simulator.spawner.CarSpawner;
import intersectionmanagement.simulator.spawner.PedestrianSpawner;
import intersectionmanagement.simulator.track.Node;
import intersectionmanagement.simulator.track.TrackParser;

import coevolution.SimulatorConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import static intersectionmanagement.simulator.spawner.CarSpawner.Function.CONSTANT;
import static intersectionmanagement.simulator.spawner.CarSpawner.Function.LINEAR;
import static intersectionmanagement.simulator.spawner.CarSpawner.Function.SIN;

public class Trial {
    private static final Logger LOGGER = Logger.getLogger(Trial.class.getName());
    // CLI option for LWJGL
    //-Djava.library.path=Trial/target/natives

    private int seed;
    private String trackFile;
    private SpawnerFactory spawnerFactory;
    private int simulationSteps;
    private byte[] serializedNetwork;

    private int pedestrianRate;
    private float pedestrianRandomness;

    private Simulator sim;
    private List<Node>  track;

    private boolean simulating = true;

    public Trial(int seed, String track, byte[] serializedNetwork)
    {
        this(seed, track, serializedNetwork, SimulatorConfig.simulationSteps,
            SimulatorConfig.spawnerType, SimulatorConfig.minPeriod, SimulatorConfig.maxPeriod, SimulatorConfig.periodMul,
            SimulatorConfig.randomness, SimulatorConfig.pedestrianRandomness, SimulatorConfig.pedestrianRate);
    }

    public Trial(int seed, List<Node> track, byte[] serializedNetwork)
    {
        this(seed, "", serializedNetwork, SimulatorConfig.simulationSteps,
            SimulatorConfig.spawnerType, SimulatorConfig.minPeriod, SimulatorConfig.maxPeriod, SimulatorConfig.periodMul,
            SimulatorConfig.randomness, SimulatorConfig.pedestrianRandomness, SimulatorConfig.pedestrianRate);

        this.track = track;
    }

    public Trial(int seed, String track, byte[] serializedNetwork, int simulationSteps,
                String spawnerType, int min_period, int max_period, double period_mul,
                double randomness, float pedestrianRandomness, int pedestrianRate)
    {
        this.seed = seed;
        this.trackFile = track;
        this.simulationSteps = simulationSteps;
        this.serializedNetwork = serializedNetwork;

        this.pedestrianRandomness = pedestrianRandomness;
        this.pedestrianRate = pedestrianRate;
        SpawnerFactory spawnerFactory;
        switch (spawnerType) {
            case "constant":
                double[] params = new double[1];
                params[0] = min_period;
                spawnerFactory = new SpawnerFactory(CONSTANT, serializedNetwork, simulationSteps, params, randomness);
                break;
            case "linear":
                params = new double[2];
                params[0] = min_period;
                params[1] = max_period;
                spawnerFactory = new SpawnerFactory(LINEAR, serializedNetwork, simulationSteps, params, randomness);
                break;
            case "sin":
                params = new double[3];
                params[0] = period_mul;
                params[1] = min_period;
                params[2] = max_period;
                spawnerFactory = new SpawnerFactory(SIN, serializedNetwork, simulationSteps, params, randomness);
                break;
            default:
                LOGGER.severe(String.format("%s is not a valid spawner type", spawnerType));
                throw new RuntimeException("No valid spawner specified in trial parameters");
        }
        this.spawnerFactory = spawnerFactory;
    }

    public void setupSim() throws IOException {
        sim = new Simulator(seed);
        for (Node startNode : track) {
            sim.addActor(spawnerFactory.getSpawner(sim, startNode));
        }
    }

    public int runSimulation() throws IOException {
        setupSim();

        for (int i = 0; i < simulationSteps; i++) {
            sim.step();
        }

        return sim.getCollisions();
    }

    public int runSimulationRendered() throws LWJGLException, IOException {
        ArrayList<Car> cars = new ArrayList<>();
        ArrayList<Pedestrian> pedestrians = new ArrayList<>();
        Renderer.setupWindow(this, 4, 800, 800);
        setupSim();

        int i=0;

        while (!Display.isCloseRequested() && simulating) {
            if (simulating) {
                sim.step();
                cars = sim.getCars();
                pedestrians = sim.getPedestrians();
            }
            Renderer.drawActors(cars, pedestrians, track);
            Renderer.handleInput(cars);

            if (Display.isCloseRequested()) {
                System.exit(0);
            }
            if (++i > simulationSteps)
                simulating = false;
        }
        Display.destroy();
        return sim.getCollisions();
    }

    public void toggleSimulating() {
        simulating = !simulating;
    }

    private class SpawnerFactory {
        private CarSpawner.Function function;
        private byte[] weights;
        private int simulationSteps;
        private double[] params;
        private double randomDenominator;

        SpawnerFactory(CarSpawner.Function function, byte[] weights, int simulationSteps, double[] params, double randomDenominator) {
            this.function = function;
            this.weights = weights;
            this.simulationSteps = simulationSteps;
            this.params = params;
            this.randomDenominator = randomDenominator;
        }

        Actor getSpawner(Simulator sim, Node startNode) {
            return new CarSpawner(sim, startNode, weights, simulationSteps, function, params, randomDenominator);
        }
    }
}
