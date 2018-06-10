package pcgapprentice.dungeonlevel.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.SimpleAction;
import pcgapprentice.dungeonlevel.DungeonDomainGenerator;
import pcgapprentice.dungeonlevel.DungeonLimitedState;

/**
 * A utility class for reading expert demonstrations from a file.
 * @author evanc
 */
public class EpisodeReader {

	/**
	 * Reads episodes from files output by the Unity-based
	 * tile level generator.
	 *
	 * @param filePaths The paths to the episode files
	 * @return A list of episodes
	 * @throws IOException
	 */
	public static List<Episode> readDatasetFromFile(String[] filePaths) throws IOException {
		List<Episode> episodes = new ArrayList<Episode>();
		for (String filePath : filePaths) {
			FileReader file = new FileReader(filePath);
			BufferedReader br = new BufferedReader(file);

			String line = "";

			Episode ep = new Episode();
			boolean hasInitialState = false;

			while((line = br.readLine()) != null) {
				// Skip empty lines
				if(line.matches("^\\s*$"))
					continue;

				String[] lineParts = line.split("\\s+");
				String action = lineParts[0];
				String stateString = lineParts[1];

				String[] stateParts = stateString.split(";");
				int x = Integer.parseInt(stateParts[0].substring(2));
				int y = Integer.parseInt(stateParts[1].substring(2));
				int availableKeys = Integer.parseInt(stateParts[2].substring(2));

				String levelString = stateParts[3].substring(2);
				String[] levelRows = levelString.split("_");
				int[][] level = new int[levelRows.length][levelRows[0].split(",").length];
				boolean hasExit = false;

				for(int i = 0; i < levelRows.length; i++) {
					String[] rowVals = levelRows[i].split(",");
					for(int j = 0; j < rowVals.length; j++) {
						int tileVal = Integer.parseInt(rowVals[j]);
						if(tileVal == DungeonDomainGenerator.CELL_EXIT)
							hasExit = true;
						level[i][j] = tileVal;
					}
				}

				DungeonLimitedState ds = new DungeonLimitedState(x, y, level, availableKeys, hasExit);

				if(!hasInitialState) {
					ep.initializeInState(ds);
					hasInitialState = true;
					continue;
				}

				ep.transition(new SimpleAction(action), ds, 0);
			}

			episodes.add(ep);
		}
		return episodes;
	}

	public static DemonstrationData readDemonstrationsFromFile(String[] filePaths) throws IOException {
		Map<String, HashMap<String, HashMap<String, Double>>> frequencies = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
		int maxEnemies = 0, maxDoors = 0, maxTreasures = 0, maxOpen = 0;
		for (String filePath : filePaths) {
			FileReader file = new FileReader(filePath);
			BufferedReader br = new BufferedReader(file);

			String line = "";

			DungeonLimitedState previousState = null;

			while((line = br.readLine()) != null) {
				// Skip empty lines
				if(line.matches("^\\s*$"))
					continue;

				String[] lineParts = line.split("\\s+");
				String action = lineParts[0];
				String stateString = lineParts[1];

				String[] stateParts = stateString.split(";");
				int x = Integer.parseInt(stateParts[0].substring(2));
				int y = Integer.parseInt(stateParts[1].substring(2));
				int availableKeys = Integer.parseInt(stateParts[2].substring(2));

				String levelString = stateParts[3].substring(2);
				String[] levelRows = levelString.split("_");
				int[][] level = new int[levelRows.length][levelRows[0].split(",").length];
				boolean hasExit = false;

				for(int i = 0; i < levelRows.length; i++) {
					String[] rowVals = levelRows[i].split(",");
					for(int j = 0; j < rowVals.length; j++) {
						int tileVal = Integer.parseInt(rowVals[j]);
						if(tileVal == DungeonDomainGenerator.CELL_EXIT)
							hasExit = true;
						level[i][j] = tileVal;
					}
				}

				DungeonLimitedState ds = new DungeonLimitedState(x, y, level, availableKeys, hasExit);

				if(ds.getDoorCount() > maxDoors)
					maxDoors = ds.getDoorCount();
				if(ds.getEnemyCount() > maxEnemies)
					maxEnemies = ds.getEnemyCount();
				if(ds.getOpenCount() > maxOpen)
					maxOpen = ds.getOpenCount();
				if(ds.getTreasureCount() > maxTreasures)
					maxTreasures = ds.getTreasureCount();

				if(previousState == null) {
					previousState = ds;
					continue;
				}

				if(!frequencies.containsKey(previousState.toString()))
					frequencies.put(previousState.toString(), new HashMap<String, HashMap<String, Double>>());
				if(!frequencies.get(previousState.toString()).containsKey(action))
					frequencies.get(previousState.toString()).put(action, new HashMap<String, Double>());
				if(!frequencies.get(previousState.toString()).get(action).containsKey(ds.toString()))
					frequencies.get(previousState.toString()).get(action).put(ds.toString(), 0.);

				// Increment the s-a-s' count
				frequencies.get(previousState.toString()).get(action).put(ds.toString(),
						frequencies.get(previousState.toString()).get(action).get(ds.toString()) + 1);


				previousState = ds;
			}

			for (String s : frequencies.keySet()) {
				for(String a : frequencies.get(s).keySet()) {
					// Sum all of the times action a was taken at state s
					double total = 0;
					for(double count : frequencies.get(s).get(a).values()) {
						total += count;
					}

					// Convert counts to frequencies
					for(Entry<String, Double> sPrime : frequencies.get(s).get(a).entrySet()) {
						String key = sPrime.getKey();
						double freq = sPrime.getValue() / total;
						frequencies.get(s).get(a).put(key, freq);
					}
				}
			}
		}

		return new DemonstrationData(maxOpen, maxDoors, maxEnemies, maxTreasures, frequencies);
	}

}
