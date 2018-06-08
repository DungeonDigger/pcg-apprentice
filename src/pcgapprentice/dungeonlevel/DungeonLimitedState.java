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

	public DungeonLimitedState(String s) {
		String[] parts = s.split(",");
		vision = new int[3][3];
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

	public DungeonLimitedState(int x, int y, int level[][], int availableKeys, boolean hasExit) {
		int enemyCount = 0;
		int treasureCount = 0;
		int doorCount = 0;
		int openCount = 0;

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

		int left = x - 1;
		int right = x + 1;
		int bottom = y - 1;
		int top = y + 1;

		int[][] visibility = new int[3][3];
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

	@Override
	public String toString() {
		String s = "";
		for(int i = 0; i < vision.length; i++)
			for(int j = 0; j < vision[0].length; j++)
				s += (vision[i][j] + ",");
		s += enemyCount + ",";
		s += treasureCount + ",";
		s += doorCount + ",";
		s += openCount + ",";
		s += availableKeys + ",";
		s += hasExit;

		return s;
	}

}
