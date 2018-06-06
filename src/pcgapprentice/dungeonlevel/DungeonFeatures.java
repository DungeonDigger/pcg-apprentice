package pcgapprentice.dungeonlevel;

import java.util.ArrayList;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.mdp.core.state.State;

public class DungeonFeatures implements DenseStateFeatures {

	@Override
	public double[] features(State s) {
		DungeonState ds = (DungeonState)s;
		int[][] level = ds.level;

		int enemyCount = 0;
		int treasureCount = 0;
		int quad1Open = 0;
		int quad2Open = 0;
		int quad3Open = 0;
		int quad4Open = 0;

		ArrayList<Double> features = new ArrayList<Double>();

		// Analyze basic features from the overall layout of the level
		for(int i = 0; i < level.length; i++) {
			for(int j = 0; j < level[0].length; j++) {
				if(level[i][j] != DungeonDomainGenerator.CELL_BLOCK) {
					if(i < 25 && j < 25) {
						quad1Open++;
					} else if(i >= 25 && j < 25) {
						quad2Open++;
					} else if(i < 25 && j >= 25) {
						quad3Open++;
					} else {
						quad4Open++;
					}
				}

				if(level[i][j] == DungeonDomainGenerator.CELL_TREASURE) {
					treasureCount++;
				} else if(level[i][j] == DungeonDomainGenerator.CELL_ENEMY) {
					enemyCount++;
				}
			}
		}

		// Limited "visibility" to the surrounding area. Represent an NxN square
		// using boolean features for whether or not they are open
		int left = ds.x - 1;
		int right = ds.x + 1;
		int bottom = ds.y - 1;
		int top = ds.y + 1;

		for(int x = left; x <= right; x++) {
			for(int y = bottom; y <= top; y++) {
				// Out of bounds squares are counted as non-traversable
				if(x < 0 || x >= ds.getLevelWidth() ||
						y < 0 || y >= ds.getLevelHeight()) {
					features.add(0.);
				} else {
					double val = level[x][y] == DungeonDomainGenerator.CELL_BLOCK ? 0 : 1;
					features.add(val);
				}
			}
		}

		// Normalization
		double quad1Norm = quad1Open / 625.0;
		double quad2Norm = quad2Open / 625.0;
		double quad3Norm = quad3Open / 625.0;
		double quad4Norm = quad4Open / 625.0;

		double enemyNorm = enemyCount / 2500.0;
		double treasureNorm = treasureCount / 2500.0;

		double hasExit = ds.hasExit ? 0 : 1;

		features.add(quad1Norm);
		features.add(quad2Norm);
		features.add(quad3Norm);
		features.add(quad4Norm);
		features.add(enemyNorm);
		features.add(treasureNorm);
		features.add(hasExit);

		double[] featureVector = features.stream().mapToDouble(Double::doubleValue).toArray();

		return featureVector;
	}

	@Override
	public DenseStateFeatures copy() {
		return new DungeonFeatures();
	}

}
