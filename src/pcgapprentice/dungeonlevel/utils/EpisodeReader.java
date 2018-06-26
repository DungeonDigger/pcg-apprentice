package pcgapprentice.dungeonlevel.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
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
	public static List<Episode> readEpisodesFromFiles(String[] filePaths, int visionRadius) throws IOException {
		List<Episode> episodes = new ArrayList<Episode>();
		for (String filePath : filePaths) {
			FileReader file = new FileReader(filePath);
			BufferedReader br = new BufferedReader(file);

			String line = "";
			List<String> actions = new ArrayList<>();
			List<DungeonLimitedState> states = new ArrayList<>();

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

				DungeonLimitedState ds = new DungeonLimitedState(x, y, level, availableKeys,
						hasExit, visionRadius);

				actions.add(action);
				states.add(ds);
			}

			// Discretize the state properties
			for (String prop :
					new String[]{DungeonDomainGenerator.VAR_ENEMY_COUNT,
							DungeonDomainGenerator.VAR_TREASURE_COUNT,
							DungeonDomainGenerator.VAR_DOOR_COUNT,
							DungeonDomainGenerator.VAR_OPEN_COUNT,
							DungeonDomainGenerator.VAR_DISTANCE}) {

				int max = Collections.max(states.stream().map(dls -> (int)dls.get(prop)).collect(Collectors.toList()));
				int partitions = prop == DungeonDomainGenerator.VAR_DISTANCE ? 5 : 3;
				double partSize = (double)max / (double)partitions;
				for (DungeonLimitedState dsl :
						states) {
					int newVal = (int)Math.floor((int)dsl.get(prop) / partSize);
					dsl.set(prop, newVal);
				}
			}

			// Initialize the episode
			ep.initializeInState(states.get(0));

			// Assemble the episode
			for(int i = 1; i < actions.size(); i++) {
				ep.transition(new SimpleAction(actions.get(i)), states.get(i), 0);
			}

			episodes.add(ep);
		}
		return episodes;
	}

	public static DemonstrationData getDemonstrationDataFromEpisodes(List<Episode> episodes) {
		Map<String, HashMap<String, HashMap<String, Double>>> frequencies = new HashMap<>();
		int maxEnemies = 0, maxDoors = 0, maxTreasures = 0, maxOpen = 0;

		for(Episode ep : episodes) {
			for(int i = 0; i < ep.stateSequence.size() - 1; i++) {
				DungeonLimitedState s = (DungeonLimitedState)ep.state(i);
				Action a = ep.action(i);
				DungeonLimitedState sprime = (DungeonLimitedState)ep.state(i + 1);

				if(!frequencies.containsKey(s.toString()))
					frequencies.put(s.toString(), new HashMap<>());
				if(!frequencies.get(s.toString()).containsKey(a.actionName()))
					frequencies.get(s.toString()).put(a.actionName(), new HashMap<>());
				if(!frequencies.get(s.toString()).get(a.actionName()).containsKey(sprime.toString()))
					frequencies.get(s.toString()).get(a.actionName()).put(sprime.toString(), 0.);

				// Increment the s-a-s' count
				frequencies.get(s.toString()).get(a.actionName()).put(sprime.toString(),
						frequencies.get(s.toString()).get(a.actionName()).get(sprime.toString()) + 1);

				if(sprime.getDoorCount() > maxDoors)
					maxDoors = sprime.getDoorCount();
				if(sprime.getEnemyCount() > maxEnemies)
					maxEnemies = sprime.getEnemyCount();
				if(sprime.getOpenCount() > maxOpen)
					maxOpen = sprime.getOpenCount();
				if(sprime.getTreasureCount() > maxTreasures)
					maxTreasures = sprime.getTreasureCount();
			}
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

		return new DemonstrationData(maxOpen, maxDoors, maxEnemies, maxTreasures, frequencies);
	}
}
