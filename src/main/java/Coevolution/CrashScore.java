package coevolution;

import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import intersectionmanagement.trial.Trial;

import org.encog.neural.neat.NEATNetwork;
import org.encog.ml.MLMethod;
import org.encog.ml.CalculateScore;

public class CrashScore
implements CalculateScore
{
	NeatController nc;

	public CrashScore(NeatController nc)
	{
		this.nc = nc;
	}

	public double calculateScore(MLMethod method)
	{
		try{
			byte net[] = SerializationUtils.serialize((NEATNetwork) method);
			Trial trial = new Trial(nc.getSeed(), nc.getTrack(), net);

			return trial.runSimulation();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}

		return Double.POSITIVE_INFINITY;
	}

	public boolean shouldMinimize()
	{
		return true;
	}

	@Override
	public boolean requireSingleThreaded()
	{
		return false;
	}
}