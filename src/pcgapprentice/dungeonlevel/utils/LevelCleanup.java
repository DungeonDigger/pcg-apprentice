package pcgapprentice.dungeonlevel.utils;

import org.apache.commons.lang3.ArrayUtils;

import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.*;

/**
 * Provides static methods for cleaning up the levels created by
 * the agent.
 */
public class LevelCleanup {

    /**
     * Gets a cleaned version of the given level.
     *
     * Cleanup consists of:
     * - Removing unnecessary doors
     *
     * @param level The level to clean
     * @return The 2D array representing the cleaned level
     */
    public static int[][] getCleanedLevel(int[][] level) {
        int[][] levelCopy = new int[level.length][level[0].length];
        for(int i = 0; i < level.length; i++)
            for(int j = 0; j < level[0].length; j++)
                levelCopy[i][j] = level[i][j];

        removeUnnecessaryDoors(levelCopy);

        return levelCopy;
    }

    /**
     * Removes any unnecessary doors from the level and replaces
     * them with untraversable blocks.
     *
     * @param level The level to remove extra doors from
     */
    private static void removeUnnecessaryDoors(int[][] level) {
        for(int i = 0; i < level.length; i++) {
            for(int j = 0; j < level[0].length; j++) {
                if(level[i][j] == CELL_DOOR) {
                    if(!isDoorNecessary(i, j, level)) {
                        level[i][j] = CELL_BLOCK;
                    }
                }
            }
        }
    }

    /**
     * Determines whether a door in a level is necessary. A door is considered necessary
     * if any portion of the level is only reachable by passing through the door.
     *
     * @param doorX The X coordinate of the door
     * @param doorY The Y coordinate of the door
     * @param level The level
     * @return True if the door uniquely blocks access to some portion of the level
     */
    private static boolean isDoorNecessary(int doorX, int doorY, int[][] level) {
        int[][] levelCopy = new int[level.length][level[0].length];
        int traversableCount = 0;
        int filledCount = 0;
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                levelCopy[i][j] = level[i][j];
                if(level[i][j] != CELL_BLOCK)
                    traversableCount++;
            }
        }

        // Find an open spot located around the door
        int openX = -1, openY = -1;
        for(int x = doorX - 1; x <= doorX + 1; x++) {
            for(int y = doorY - 1; y <= doorY + 1; y++) {
                // Don't consider spaces that are out of bounds or the door itslef
                if(x < 0 || x >= level.length || y < 0 || y >= level[0].length ||
                        (x == doorX && y == doorY))
                    continue;
                if(levelCopy[x][y] != CELL_BLOCK) {
                    openX = x;
                    openY = y;
                    break;
                }
            }
            if(openX != -1) break;
        }

        // Convert the door under consideration to a block to make it non-traversable
        levelCopy[doorX][doorY] = CELL_BLOCK;
        traversableCount--;

        // Perform a flood fill from the open spot we identified
        LevelMetrics.floodFill(openX, openY, levelCopy, false);

        // Count the number of filled in tiles. If every traversable tile is filled, it means
        // that the door was not uniquely blocking off any region of the level, and can
        // be considered unnecessary
        for (int i = 0; i < levelCopy.length; i++) {
            for (int j = 0; j < levelCopy[0].length; j++) {
                if(levelCopy[i][j] == LevelMetrics.FILL_COLOR)
                    filledCount++;
            }
        }

        return filledCount < traversableCount;
    }
}
