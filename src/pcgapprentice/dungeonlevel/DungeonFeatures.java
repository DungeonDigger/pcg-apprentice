package pcgapprentice.dungeonlevel;

import java.util.ArrayList;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.mdp.core.state.State;

public class DungeonFeatures implements DenseStateFeatures {

	public int maxEnemies;
	public int maxTreasures;
	public int maxDoors;
	public int maxOpen;

	public DungeonFeatures(int maxEnemies, int maxTreasures, int maxDoors, int maxOpen) {
		super();
		this.maxEnemies = maxEnemies;
		this.maxTreasures = maxTreasures;
		this.maxDoors = maxDoors;
		this.maxOpen = maxOpen;
	}

	@Override
	public double[] features(State s) {
		DungeonLimitedState ds = (DungeonLimitedState)s;
		ArrayList<Double> features = new ArrayList<Double>();

		// Add the bias term
		features.add(1.0);

		// Normalize values by the maximum value encountered for each in the training data
		features.add((double)ds.enemyCount / (double)maxEnemies);
		features.add((double)ds.treasureCount / (double)maxTreasures);
		features.add((double)ds.doorCount / (double)maxDoors);
		features.add((double)ds.openCount / (double)maxOpen);
		features.add(ds.hasExit ? 1.0 : 0);
//		features.add(ds.sensorNorth || ds.sensorSouth || ds.sensorEast || ds.sensorWest ? 1.0 : 0);

		double[] featureVector = features.stream().mapToDouble(Double::doubleValue).toArray();

		return featureVector;
	}

	@Override
	public DenseStateFeatures copy() {
		return new DungeonFeatures(maxEnemies, maxTreasures, maxDoors, maxOpen);
	}

}
