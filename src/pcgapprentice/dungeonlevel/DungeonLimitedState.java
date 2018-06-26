package pcgapprentice.dungeonlevel;

import java.util.*;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.DeepCopyState;

@DeepCopyState
public class DungeonLimitedState implements MutableState {

	int[][] vision;
	int enemyCount;
	int treasureCount;
	int doorCount;
	int openCount;
	int availableKeys;
	boolean hasExit;
	boolean roomWouldIntersect;
	boolean sensorNorth;
	boolean sensorSouth;
	boolean sensorEast;
	boolean sensorWest;
	int distance;

	private final static List<Object> keys = Arrays.asList(
			DungeonDomainGenerator.VAR_VISION,
			DungeonDomainGenerator.VAR_ENEMY_COUNT,
			DungeonDomainGenerator.VAR_TREASURE_COUNT,
			DungeonDomainGenerator.VAR_DOOR_COUNT,
			DungeonDomainGenerator.VAR_OPEN_COUNT,
			DungeonDomainGenerator.VAR_AVAILABLE_KEYS,
			DungeonDomainGenerator.VAR_HAS_EXIT,
			DungeonDomainGenerator.VAR_ROOM_WOULD_INTERSECT,
			DungeonDomainGenerator.VAR_SENSOR_NORTH,
			DungeonDomainGenerator.VAR_SENSOR_SOUTH,
			DungeonDomainGenerator.VAR_SENSOR_EAST,
			DungeonDomainGenerator.VAR_SENSOR_WEST,
			DungeonDomainGenerator.VAR_DISTANCE
		);

	public DungeonLimitedState() {}

	public DungeonLimitedState(int[][] vision, int enemyCount, int treasureCount, int doorCount, int openCount,
							   int availableKeys, boolean hasExit, boolean roomWouldIntersect, boolean sensorNorth,
							   boolean sensorSouth, boolean sensorEast, boolean sensorWest, int distance) {
		this.vision = vision;
		this.enemyCount = enemyCount;
		this.treasureCount = treasureCount;
		this.doorCount = doorCount;
		this.openCount = openCount;
		this.availableKeys = availableKeys;
		this.hasExit = hasExit;
		this.roomWouldIntersect = roomWouldIntersect;
		this.sensorNorth = sensorNorth;
		this.sensorSouth = sensorSouth;
		this.sensorEast = sensorEast;
		this.sensorWest = sensorWest;
		this.distance = distance;
	}

	public DungeonLimitedState(String s, int visionRadius) {
		int visionDim = getVisionDim(visionRadius);
		String[] parts = s.split(",");
		vision = new int[visionDim][visionDim];
		int partIx = 0;
		for(int i = 0; i < vision.length; i++) {
			for(int j = 0; j < vision[0].length; j++) {
				vision[i][j] = Integer.parseInt(parts[partIx]);
				partIx++;
			}
		}

		enemyCount = Integer.parseInt(parts[partIx]);
		partIx++;
		treasureCount = Integer.parseInt(parts[partIx]);
		partIx++;
		doorCount = Integer.parseInt(parts[partIx]);
		partIx++;
		openCount = Integer.parseInt(parts[partIx]);
		partIx++;
		availableKeys = Integer.parseInt(parts[partIx]);
		partIx++;
		hasExit = Boolean.parseBoolean(parts[partIx]);
		partIx++;
		roomWouldIntersect = Boolean.parseBoolean(parts[partIx]);
		partIx++;
		sensorNorth = Boolean.parseBoolean(parts[partIx]);
		partIx++;
		sensorSouth = Boolean.parseBoolean(parts[partIx]);
		partIx++;
		sensorEast = Boolean.parseBoolean(parts[partIx]);
		partIx++;
		sensorWest = Boolean.parseBoolean(parts[partIx]);
		partIx++;
		distance = Integer.parseInt(parts[partIx]);
	}

	public DungeonLimitedState(int x, int y, int level[][], int availableKeys, boolean hasExit,
							   int visionRadius) {
		int enemyCount = 0;
		int treasureCount = 0;
		int doorCount = 0;
		int openCount = 0;
		int visionDim = getVisionDim(visionRadius);

		for(int i = 0; i < level.length; i++) {
			for(int j = 0; j < level[0].length; j++) {
				if(level[i][j] != DungeonDomainGenerator.CELL_BLOCK) {
					openCount++;
				}

				if(level[i][j] == DungeonDomainGenerator.CELL_TREASURE) {
					treasureCount++;
				} else if(level[i][j] == DungeonDomainGenerator.CELL_ENEMY) {
					enemyCount++;
				} else if(level[i][j] == DungeonDomainGenerator.CELL_DOOR) {
					doorCount++;
				}
			}
		}

		int left = x - visionRadius;
		int right = x + visionRadius;
		int bottom = y - visionRadius;
		int top = y + visionRadius;

		int[][] visibility = new int[visionDim][visionDim];
		int i = 0, j = 0;
		for(int dx = left; dx <= right; dx++) {
			j = 0;
			for(int dy = bottom; dy <= top; dy++) {
				// Out of bounds squares are counted as non-traversable
				if(dx < 0 || dx >= level.length ||
						dy < 0 || dy >= level[0].length) {
					visibility[i][j] = DungeonDomainGenerator.CELL_VOID;
				} else {
					visibility[i][j] = level[dx][dy];
				}
				j++;
			}
			i++;
		}

		boolean intersect = false;
		left = x - 4;
		right = x + 4;
		bottom = y - 4;
		top = y + 4;

		for(int a = left; a <= right; a++) {
			for(int b = bottom; b <= top; b++) {
				if(a < 0 || a >= level.length ||
						b < 0 || b >= level[0].length) {
					// out of bounds is counted as a room intersection since
					// the full room cannot be placed
					intersect = true;
					break;
				}
				if(a != x && y != b && level[a][b] != DungeonDomainGenerator.CELL_BLOCK) {
					intersect = true;
					break;
				}
			}
			if(intersect) break;
		}

		int distanceFromStart = getDijkstraShortestPathDistance(level, 24, 0, x, y);

		this.vision = visibility;
		this.enemyCount = enemyCount;
		this.treasureCount = treasureCount;
		this.doorCount = doorCount;
		this.openCount = openCount;
		this.availableKeys = availableKeys;
		this.hasExit = hasExit;
		this.roomWouldIntersect = intersect;
		this.sensorNorth = y > level[0].length - 4;
		this.sensorSouth = y < 3;
		this.sensorEast = x > level.length - 4;
		this.sensorWest = x < 3;
		this.distance = distanceFromStart;
	}

	/**
	 * Gets the dimension of the vision 2D array from the given radius.
	 *
	 * @param visionRadius The radius of vision the agent should have
	 * @return The radius of vision
	 */
	private int getVisionDim(int visionRadius) {
		return visionRadius * 2 + 1;
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
	private int getDijkstraShortestPathDistance(int[][] level, int startX, int startY, int endX, int endY) {
		// Collect all of the traversable tiles
		List<String> unvisited = new ArrayList<>();
		for(int i = 0; i < level.length; i++)
			for(int j = 0; j < level[0].length; j++)
				if (level[i][j] != DungeonDomainGenerator.CELL_BLOCK) unvisited.add(i + "_" + j);

		Map<String, Integer> distances = new HashMap<>();
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
				return distances.get(toVisit);

			// Visit all of the nodes neighbors and update their distances
			int x = Integer.parseInt(toVisit.split("_")[0]);
			int y = Integer.parseInt(toVisit.split("_")[1]);
			List<String> neighbors = new ArrayList<>();
			if (x - 1 > 0 && level[x - 1][y] != DungeonDomainGenerator.CELL_BLOCK)
				neighbors.add((x - 1) + "_" + y);
			if (x + 1 < level.length && level[x + 1][y] != DungeonDomainGenerator.CELL_BLOCK)
				neighbors.add((x + 1) + "_" + y);
			if (y - 1 > 0 && level[x][y - 1] != DungeonDomainGenerator.CELL_BLOCK)
				neighbors.add(x + "_" + (y - 1));
			if (y + 1 < level[0].length && level[x][y + 1] != DungeonDomainGenerator.CELL_BLOCK)
				neighbors.add(x + "_" + (y + 1));

			int altDistance = distances.get(toVisit) + 1;
			for(String neighbor : neighbors) {
				if(altDistance < distances.get(neighbor))
					distances.put(neighbor, altDistance);
			}
		}

		// Sad times, we didn't find our destination
		System.out.println("Failed to find destination in shortest path");
		return -1;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		switch(variableKey.toString()) {
			case DungeonDomainGenerator.VAR_VISION:
				return vision;
			case DungeonDomainGenerator.VAR_ENEMY_COUNT:
				return enemyCount;
			case DungeonDomainGenerator.VAR_TREASURE_COUNT:
				return treasureCount;
			case DungeonDomainGenerator.VAR_DOOR_COUNT:
				return doorCount;
			case DungeonDomainGenerator.VAR_OPEN_COUNT:
				return openCount;
			case DungeonDomainGenerator.VAR_AVAILABLE_KEYS:
				return availableKeys;
			case DungeonDomainGenerator.VAR_HAS_EXIT:
				return hasExit;
			case DungeonDomainGenerator.VAR_ROOM_WOULD_INTERSECT:
				return roomWouldIntersect;
			case DungeonDomainGenerator.VAR_SENSOR_NORTH:
				return sensorNorth;
			case DungeonDomainGenerator.VAR_SENSOR_SOUTH:
				return sensorSouth;
			case DungeonDomainGenerator.VAR_SENSOR_EAST:
				return sensorEast;
			case DungeonDomainGenerator.VAR_SENSOR_WEST:
				return sensorWest;
			case DungeonDomainGenerator.VAR_DISTANCE:
				return distance;
			default:
				throw new UnknownKeyException(variableKey);
		}
	}

	@Override
	public State copy() {
		int[][] visionCopy = new int[vision.length][vision[0].length];
		for(int i = 0; i < vision.length; i++)
			for(int j = 0; j < vision[0].length; j++)
				visionCopy[i][j] = vision[i][j];
		return new DungeonLimitedState(visionCopy, enemyCount, treasureCount, doorCount, openCount, availableKeys,
				hasExit, roomWouldIntersect, sensorNorth, sensorSouth, sensorEast, sensorWest, distance);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		switch(variableKey.toString()) {
			case DungeonDomainGenerator.VAR_VISION:
				this.vision = (int[][])value;
				break;
			case DungeonDomainGenerator.VAR_ENEMY_COUNT:
				enemyCount = StateUtilities.stringOrNumber(value).intValue();
				break;
			case DungeonDomainGenerator.VAR_TREASURE_COUNT:
				treasureCount = StateUtilities.stringOrNumber(value).intValue();
				break;
			case DungeonDomainGenerator.VAR_DOOR_COUNT:
				doorCount = StateUtilities.stringOrNumber(value).intValue();
				break;
			case DungeonDomainGenerator.VAR_OPEN_COUNT:
				openCount = StateUtilities.stringOrNumber(value).intValue();
				break;
			case DungeonDomainGenerator.VAR_AVAILABLE_KEYS:
				availableKeys = StateUtilities.stringOrNumber(value).intValue();
				break;
			case DungeonDomainGenerator.VAR_HAS_EXIT:
				hasExit = StateUtilities.stringOrBoolean(value).booleanValue();
				break;
			case DungeonDomainGenerator.VAR_ROOM_WOULD_INTERSECT:
				roomWouldIntersect = StateUtilities.stringOrBoolean(value).booleanValue();
				break;
			case DungeonDomainGenerator.VAR_SENSOR_NORTH:
				sensorNorth = StateUtilities.stringOrBoolean(value).booleanValue();
				break;
			case DungeonDomainGenerator.VAR_SENSOR_SOUTH:
				sensorSouth = StateUtilities.stringOrBoolean(value).booleanValue();
				break;
			case DungeonDomainGenerator.VAR_SENSOR_EAST:
				sensorEast = StateUtilities.stringOrBoolean(value).booleanValue();
				break;
			case DungeonDomainGenerator.VAR_SENSOR_WEST:
				sensorWest = StateUtilities.stringOrBoolean(value).booleanValue();
				break;
			case DungeonDomainGenerator.VAR_DISTANCE:
				distance = StateUtilities.stringOrNumber(value).intValue();
				break;
			default:
				throw new UnknownKeyException(variableKey);
		}
		return this;
	}

	/**
	 * Gets a string representation of the agent's field of vision.
	 * @return A comma delimited string
	 */
	public String getVisionString() {
		String s = "";
		for(int i = 0; i < vision.length; i++)
			for(int j = 0; j < vision[0].length; j++)
				s += (vision[i][j] + ",");
		return s.substring(0, s.length() - 1);
	}

	@Override
	public String toString() {
		String s = getVisionString() + ",";
		s += enemyCount + ",";
		s += treasureCount + ",";
		s += doorCount + ",";
		s += openCount + ",";
		s += availableKeys + ",";
		s += hasExit + ",";
		s += roomWouldIntersect + ",";
		s += sensorNorth + ",";
		s += sensorSouth + ",";
		s += sensorEast + ",";
		s += sensorWest + ",";
		s += distance;

		return s;
	}

	public int[][] getVision() {
		return vision;
	}

	public void setVision(int[][] vision) {
		this.vision = vision;
	}

	public int getEnemyCount() {
		return enemyCount;
	}

	public void setEnemyCount(int enemyCount) {
		this.enemyCount = enemyCount;
	}

	public int getTreasureCount() {
		return treasureCount;
	}

	public void setTreasureCount(int treasureCount) {
		this.treasureCount = treasureCount;
	}

	public int getDoorCount() {
		return doorCount;
	}

	public void setDoorCount(int doorCount) {
		this.doorCount = doorCount;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public int getAvailableKeys() {
		return availableKeys;
	}

	public void setAvailableKeys(int availableKeys) {
		this.availableKeys = availableKeys;
	}

	public boolean isHasExit() {
		return hasExit;
	}

	public void setHasExit(boolean hasExit) {
		this.hasExit = hasExit;
	}

	public boolean isRoomWouldIntersect() {
		return roomWouldIntersect;
	}

	public void setRoomWouldIntersect(boolean roomWouldIntersect) {
		this.roomWouldIntersect = roomWouldIntersect;
	}

	public boolean isSensorNorth() {
		return sensorNorth;
	}

	public void setSensorNorth(boolean sensorNorth) {
		this.sensorNorth = sensorNorth;
	}

	public boolean isSensorSouth() {
		return sensorSouth;
	}

	public void setSensorSouth(boolean sensorSouth) {
		this.sensorSouth = sensorSouth;
	}

	public boolean isSensorEast() {
		return sensorEast;
	}

	public void setSensorEast(boolean sensorEast) {
		this.sensorEast = sensorEast;
	}

	public boolean isSensorWest() {
		return sensorWest;
	}

	public void setSensorWest(boolean sensorWest) {
		this.sensorWest = sensorWest;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public static List<Object> getKeys() {
		return keys;
	}

}
