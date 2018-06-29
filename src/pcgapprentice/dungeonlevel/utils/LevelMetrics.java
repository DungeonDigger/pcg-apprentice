package pcgapprentice.dungeonlevel.utils;

import javafx.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import pcgapprentice.dungeonlevel.DungeonDomainGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

/**
 * A collection of methods for static evaluation of dungeon levels.
 */
public class LevelMetrics {

    private static final int FILL_COLOR = 100;

    /**
     * Gets an estimate of the percentage of the level that will be
     * explored before the exit is reached. This is approximated by
     * performing a flood fill from the starting point of all traversable
     * squares which terminates upon reaching the exit.
     *
     * @param level The level to examine
     * @param startX Starting x loc
     * @param startY Starting y loc
     * @return A value between 0 and 1 representing the percentage of tiles
     * explored.
     */
    public static double getExplorationPercentage(int[][] level, int startX, int startY) {
        int[][] levelCopy = new int[level.length][level[0].length];
        int traversableCount = 0;
        int filledCount = 0;
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                levelCopy[i][j] = level[i][j];
                if(level[i][j] != DungeonDomainGenerator.CELL_BLOCK)
                    traversableCount++;
            }
        }

        floodFill(startX, startY, level);

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                if(level[i][j] == FILL_COLOR)
                    filledCount++;
            }
        }

        return (double)filledCount / (double)traversableCount;
    }

    /**
     * Reads a level tilemap file into a 2D array.
     *
     * @param filePath Path to the level file
     * @return The output level
     * @throws IOException
     */
    public static int[][] readLevelFromFile(String filePath) throws IOException {
        FileReader file = new FileReader(filePath);
        BufferedReader br = new BufferedReader(file);

        String line = "";
        int[][] level = new int[50][50];
        int rowIx = 49;
        while((line = br.readLine()) != null) {
            // Skip empty lines
            if (line.matches("^\\s*$"))
                continue;
            int[] row = Arrays.stream(line.split("\\s+")).mapToInt(Integer::parseInt).toArray();
            for (int colIx = 0; colIx < row.length; colIx++) {
                level[colIx][rowIx] = row[colIx];
            }
            rowIx--;
        }
        return level;
    }

    private static void floodFill(int nodeX, int nodeY, int[][] level) {
        if(level[nodeX][nodeY] == FILL_COLOR) return;
        if(level[nodeX][nodeY] == DungeonDomainGenerator.CELL_BLOCK) return;
        Queue<Pair<Integer, Integer>> queue = new ArrayDeque<>();

        level[nodeX][nodeY] = FILL_COLOR;
        queue.offer(new Pair<>(nodeX, nodeY));
        while(!queue.isEmpty()) {
            Pair<Integer, Integer> n = queue.poll();
            int nx = n.getKey();
            int ny = n.getValue();

            // South
            if(nodeY + 1 < level[0].length && level[nx][ny + 1] != DungeonDomainGenerator.CELL_BLOCK
                    && level[nx][ny + 1] != FILL_COLOR) {
                if(level[nx][ny + 1] == DungeonDomainGenerator.CELL_EXIT) {
                    level[nx][ny + 1] = FILL_COLOR;
                    break;
                }
                level[nx][ny + 1] = FILL_COLOR;
                queue.offer(new Pair<>(nx, ny + 1));
            }
            // North
            if(nodeY - 1 >= 0  && level[nx][ny - 1] != DungeonDomainGenerator.CELL_BLOCK
                    && level[nx][ny - 1] != FILL_COLOR) {
                if(level[nx][ny - 1] == DungeonDomainGenerator.CELL_EXIT) {
                    level[nx][ny - 1] = FILL_COLOR;
                    break;
                }
                level[nx][ny - 1] = FILL_COLOR;
                queue.offer(new Pair<>(nx, ny - 1));
            }
            // East
            if(nodeX + 1 < level.length  && level[nx + 1][ny] != DungeonDomainGenerator.CELL_BLOCK
                    && level[nx + 1][ny] != FILL_COLOR) {
                if(level[nx + 1][ny] == DungeonDomainGenerator.CELL_EXIT) {
                    level[nx + 1][ny] = FILL_COLOR;
                    break;
                }
                level[nx + 1][ny] = FILL_COLOR;
                queue.offer(new Pair<>(nx + 1, ny));
            }
            // West
            if(nodeX - 1 >= 0  && level[nx - 1][ny] != DungeonDomainGenerator.CELL_BLOCK
                    && level[nx - 1][ny] != FILL_COLOR) {
                if(level[nx - 1][ny] == DungeonDomainGenerator.CELL_EXIT) {
                    level[nx - 1][ny] = FILL_COLOR;
                    break;
                }
                level[nx - 1][ny] = FILL_COLOR;
                queue.offer(new Pair<>(nx - 1, ny));
            }
        }
    }

}
