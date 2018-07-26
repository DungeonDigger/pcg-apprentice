package pcgapprentice.dungeonlevel.utils;

import javafx.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import pcgapprentice.dungeonlevel.DungeonDomainGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.*;

/**
 * A collection of methods for static evaluation of dungeon levels.
 */
public class LevelMetrics {

    public static final int FILL_COLOR = 100;

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
                if(level[i][j] != CELL_BLOCK)
                    traversableCount++;
            }
        }

        floodFill(startX, startY, levelCopy);

        for (int i = 0; i < levelCopy.length; i++) {
            for (int j = 0; j < levelCopy[0].length; j++) {
                if(levelCopy[i][j] == FILL_COLOR)
                    filledCount++;
            }
        }

        return (double)filledCount / (double)traversableCount;
    }

    /**
     * Gets a metric of the linearity of the shortest path from start to end in a level,
     * not considering the need to collect keys for doors.
     *
     * This is calculated as 1 divided by the number of direction changes in the shortest
     * path from start to end.
     *
     * @param level
     * @param startX
     * @param startY
     * @return
     */
    public static double getLinearity(int[][] level, int startX, int startY) {
        int endX = -1, endY = -1;
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                if(level[i][j] == CELL_EXIT) {
                    endX = i;
                    endY = j;
                    break;
                }
            }
            if(endX != -1) break;
        }
        List<Pair<Integer, Integer>> path = getDijkstraShortestPath(level, startX, startY, endX, endY);
        String lastDir = "";
        int directionChanges = 0;
        Pair<Integer, Integer> prev = path.get(0);
        path.remove(0);
        for(Pair<Integer, Integer> node : path) {
            String dir = "";
            if(node.getKey() ==  prev.getKey() + 1)
                dir = ACTION_RIGHT;
            else if (node.getKey() == prev.getKey() - 1)
                dir = ACTION_LEFT;
            else if (node.getValue() == prev.getValue() + 1)
                dir = ACTION_UP;
            else
                dir = ACTION_DOWN;

            if(lastDir != dir)
                directionChanges++;
            lastDir = dir;
        }

        return 1. / (double)directionChanges;
    }

    /**
     * Gets the leniency of a level. This evaluated as the sigmoid of the level
     * score, where score is:
     * +1 for each treasure
     * -0.25 for each enemy
     * -1 for each door in the shortest path
     *
     * @param level The level
     * @param startX Start x loc
     * @param startY Start y loc
     * @return The leniency
     */
    public static double getLeniency(int[][] level, int startX, int startY) {
        int endX = -1, endY = -1;
        double score = 0;
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                if(level[i][j] == CELL_EXIT) {
                    endX = i;
                    endY = j;
                } else if(level[i][j] == CELL_ENEMY) {
                    score -= 0.25;
                } else if(level[i][j] == CELL_TREASURE) {
                    // Treasures are easy points!
                    score++;
                }
            }
        }

        // Doors blocking our shortest path detract from leniency
        List<Pair<Integer, Integer>> path = getDijkstraShortestPath(level, startX, startY, endX, endY);
        for(Pair<Integer, Integer> node : path) {
            if(level[node.getKey()][node.getValue()] == CELL_DOOR) {
                score--;
            }
        }

        return sigmoid(score);
    }

    /**
     * Gets the density of "stuff" (e.g. enemies, treasures, keys) in a level.
     *
     * @param level The level
     * @return Density
     */
    public static double getDensity(int[][] level) {
        double open = 0., thing = 0.;
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[0].length; j++) {
                if(level[i][j] == CELL_OPEN) {
                    open++;
                } else if(level[i][j] != CELL_BLOCK && level[i][j] != CELL_EXIT) {
                    open++;
                    thing++;
                }
            }
        }
        return thing / open;
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

    /**
     * Gets the shortest path distance between the start and end points in a level.
     * All non-block cells are considered traversable.
     *
     * @param level The tilemap of the level
     * @param startX Start location X
     * @param startY Start location Y
     * @param endX Destination X
     * @param endY Destination Y
     * @return The distance from the start to the end point.
     */
    public static int getDijkstraShortestPathDistance(int[][] level, int startX, int startY, int endX, int endY) {
        return dijkstraShortestPath(level, startX, startY, endX, endY).getKey();
    }

    /**
     * Gets the shortest path between the start and endpoints of a level. Considers
     * all non-blocks as traversable and does not consider needing to obtain keys
     * for doors.
     *
     * @param level The tilemap of the level
     * @param startX Start location X
     * @param startY Start location Y
     * @param endX Destination X
     * @param endY Destination Y
     * @return The path from the start to the end point.
     */
    public static List<Pair<Integer, Integer>> getDijkstraShortestPath(int[][] level, int startX, int startY, int endX,
                                                                       int endY) {
        Map<String, String> prev = dijkstraShortestPath(level, startX, startY, endX, endY).getValue();
        List<Pair<Integer, Integer>> path = new ArrayList<>();
        String node = endX + "_" + endY;
        while(prev.containsKey(node)) {
            int x = Integer.parseInt(node.split("_")[0]);
            int y = Integer.parseInt(node.split("_")[1]);
            path.add(0, new Pair<>(x, y));
            node = prev.get(node);
        }
        int x = Integer.parseInt(node.split("_")[0]);
        int y = Integer.parseInt(node.split("_")[1]);
        path.add(0, new Pair<>(x, y));
        return path;
    }

    private static Pair<Integer, Map<String, String>> dijkstraShortestPath(int[][] level, int startX, int startY, int endX, int endY) {
        // Collect all of the traversable tiles
        List<String> unvisited = new ArrayList<>();
        for(int i = 0; i < level.length; i++)
            for(int j = 0; j < level[0].length; j++)
                if (level[i][j] != CELL_BLOCK) unvisited.add(i + "_" + j);

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for(String s : unvisited)
            distances.put(s, Integer.MAX_VALUE);
        distances.put(startX + "_" + startY, 0);
        String targetLoc = endX + "_" + endY;

        while(!unvisited.isEmpty()) {
            // Find the unvisited node with the shortest distance
            String toVisit = null;
            for(String v : unvisited) {
                if(toVisit == null || distances.get(v) < distances.get(toVisit)) {
                    toVisit = v;
                }
            }
            unvisited.remove(toVisit);

            if(toVisit.equals(targetLoc))
                break;

            // Visit all of the nodes neighbors and update their distances
            int x = Integer.parseInt(toVisit.split("_")[0]);
            int y = Integer.parseInt(toVisit.split("_")[1]);
            List<String> neighbors = new ArrayList<>();
            if (x - 1 > 0 && level[x - 1][y] != CELL_BLOCK)
                neighbors.add((x - 1) + "_" + y);
            if (x + 1 < level.length && level[x + 1][y] != CELL_BLOCK)
                neighbors.add((x + 1) + "_" + y);
            if (y - 1 > 0 && level[x][y - 1] != CELL_BLOCK)
                neighbors.add(x + "_" + (y - 1));
            if (y + 1 < level[0].length && level[x][y + 1] != CELL_BLOCK)
                neighbors.add(x + "_" + (y + 1));

            int altDistance = distances.get(toVisit) + 1;
            for(String neighbor : neighbors) {
                if(altDistance < distances.get(neighbor)) {
                    distances.put(neighbor, altDistance);
                    prev.put(neighbor, toVisit);
                }
            }
        }

        return new Pair<>(distances.get(targetLoc), prev);
    }

    public static void floodFill(int nodeX, int nodeY, int[][] level) {
        floodFill(nodeX, nodeY, level, true);
    }

    public static void floodFill(int nodeX, int nodeY, int[][] level, boolean stopAtExit) {
        if(level[nodeX][nodeY] == FILL_COLOR) return;
        if(level[nodeX][nodeY] == CELL_BLOCK) return;
        Queue<Pair<Integer, Integer>> queue = new ArrayDeque<>();

        level[nodeX][nodeY] = FILL_COLOR;
        queue.offer(new Pair<>(nodeX, nodeY));
        while(!queue.isEmpty()) {
            Pair<Integer, Integer> n = queue.poll();
            int nx = n.getKey();
            int ny = n.getValue();

            // South
            if(ny + 1 < level[0].length && (level[nx][ny + 1] != CELL_BLOCK)
                    && (level[nx][ny + 1] != FILL_COLOR)) {
                if(level[nx][ny + 1] == CELL_EXIT && stopAtExit) {
                    level[nx][ny + 1] = FILL_COLOR;
                    break;
                }
                level[nx][ny + 1] = FILL_COLOR;
                queue.offer(new Pair<>(nx, ny + 1));
            }
            // North
            if(ny - 1 >= 0  && (level[nx][ny - 1] != CELL_BLOCK)
                    && (level[nx][ny - 1] != FILL_COLOR)) {
                if(level[nx][ny - 1] == CELL_EXIT && stopAtExit) {
                    level[nx][ny - 1] = FILL_COLOR;
                    break;
                }
                level[nx][ny - 1] = FILL_COLOR;
                queue.offer(new Pair<>(nx, ny - 1));
            }
            // East
            if(nx + 1 < level.length && (level[nx + 1][ny] != CELL_BLOCK)
                    && (level[nx + 1][ny] != FILL_COLOR)) {
                if(level[nx + 1][ny] == CELL_EXIT && stopAtExit) {
                    level[nx + 1][ny] = FILL_COLOR;
                    break;
                }
                level[nx + 1][ny] = FILL_COLOR;
                queue.offer(new Pair<>(nx + 1, ny));
            }
            // West
            if(nx - 1 >= 0  && (level[nx - 1][ny] != CELL_BLOCK)
                    && (level[nx - 1][ny] != FILL_COLOR)) {
                if(level[nx - 1][ny] == CELL_EXIT && stopAtExit) {
                    level[nx - 1][ny] = FILL_COLOR;
                    break;
                }
                level[nx - 1][ny] = FILL_COLOR;
                queue.offer(new Pair<>(nx - 1, ny));
            }
        }
    }

    private static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

}
