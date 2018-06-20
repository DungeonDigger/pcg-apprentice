package pcgapprentice.dungeonlevel;

import java.util.Arrays;
import java.util.List;

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

	private final static List<Object> keys = Arrays.<Object>asList(
			DungeonDomainGenerator.VAR_VISION,
			DungeonDomainGenerator.VAR_ENEMY_COUNT,
			DungeonDomainGenerator.VAR_TREASURE_COUNT,
			DungeonDomainGenerator.VAR_DOOR_COUNT,
			DungeonDomainGenerator.VAR_OPEN_COUNT,
			DungeonDomainGenerator.VAR_AVAILABLE_KEYS,
			DungeonDomainGenerator.VAR_HAS_EXIT
		);

	public DungeonLimitedState() {}

	public DungeonLimitedState(int[][] vision, int enemyCount, int treasureCount, int doorCount, int openCount,
			int availableKeys, boolean hasExit) {
		super();
		this.vision = vision;
		this.enemyCount = enemyCount;
		this.treasureCount = treasureCount;
		this.doorCount = doorCount;
		this.openCount = openCount;
		this.availableKeys = availableKeys;
		this.hasExit = hasExit;
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

		this.vision = visibility;
		this.enemyCount = enemyCount;
		this.treasureCount = treasureCount;
		this.doorCount = doorCount;
		this.openCount = openCount;
		this.availableKeys = availableKeys;
		this.hasExit = hasExit;
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
		return new DungeonLimitedState(visionCopy, enemyCount, treasureCount, doorCount, openCount, availableKeys, hasExit);
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
				hasExit = StateUtilities.stringOrBoolean(value).booleanValue();;
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
		s += hasExit;

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

	public static List<Object> getKeys() {
		return keys;
	}



}
