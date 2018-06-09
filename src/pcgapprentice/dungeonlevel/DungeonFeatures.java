package pcgapprentice.dungeonlevel;

import java.util.ArrayList;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.mdp.core.state.State;

public class DungeonFeatures implements DenseStateFeatures {

	@Override
	public double[] features(State s) {
		DungeonLimitedState ds = (DungeonLimitedState)s;
		ArrayList<Double> features = new ArrayList<Double>();

		// TODO should these be normalized?
		features.add((double)ds.enemyCount);
		features.add((double)ds.treasureCount);
		features.add((double)ds.doorCount);
		features.add((double)ds.openCount);
		features.add(ds.hasExit ? 1.0 : 0);

		double[] featureVector = features.stream().mapToDouble(Double::doubleValue).toArray();

		return featureVector;
	}

	@Override
	public DenseStateFeatures copy() {
		return new DungeonFeatures();
	}

}
