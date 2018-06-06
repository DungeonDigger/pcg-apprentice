package pcgapprentice.dungeonlevel.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.SimpleAction;
import pcgapprentice.dungeonlevel.DungeonDomainGenerator;
import pcgapprentice.dungeonlevel.DungeonState;

/**
 * A utility class for reading expert demonstrations from a file.
 * @author evanc
 */
public class EpisodeReader {

	/**
	 * Reads an episode from a file output by the Unity-based
	 * tile level generator.
	 *
	 * @param filePath The path to the file
	 * @return The Episode
	 * @throws IOException
	 */
	public static Episode readDatasetFromFile(String filePath) throws IOException {
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

			DungeonState ds = new DungeonState(x, y, level, availableKeys, hasExit);

			if(!hasInitialState) {
				ep.initializeInState(ds);
				hasInitialState = true;
				continue;
			}

			ep.transition(new SimpleAction(action), ds, 0);
		}

		return ep;
	}

}
