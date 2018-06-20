package pcgapprentice.dungeonlevel;

import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_DOOR;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_DOWN;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_ENEMY;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_EXIT;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_KEY;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_LEFT;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_RIGHT;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_ROOM_LARGE;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_ROOM_MED;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_ROOM_SMALL;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_TREASURE;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.ACTION_UP;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.CELL_BLOCK;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.CELL_DOOR;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.CELL_ENEMY;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.CELL_EXIT;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.CELL_KEY;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.CELL_OPEN;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.CELL_TREASURE;

import java.util.List;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

public class DungeonStateModel implements FullStateModel {

	@Override
	public State sample(State s, Action a) {
		DungeonState nextState = (DungeonState)s.copy();
		String action = a.actionName();
		switch(action) {
			case ACTION_UP:
			case ACTION_DOWN:
			case ACTION_LEFT:
			case ACTION_RIGHT:
				move(nextState, action);
				break;
			case ACTION_ROOM_SMALL:
				createRoom(nextState, 3);
				break;
			case ACTION_ROOM_MED:
				createRoom(nextState, 5);
				break;
			case ACTION_ROOM_LARGE:
				createRoom(nextState, 7);
				break;
			case ACTION_TREASURE:
				setDiggerTile(nextState, CELL_TREASURE);
				break;
			case ACTION_ENEMY:
				setDiggerTile(nextState, CELL_ENEMY);
				break;
			case ACTION_KEY:
				setDiggerTile(nextState, CELL_KEY);
				nextState.availableKeys++;
				break;
			case ACTION_DOOR:
				if(nextState.availableKeys > 0) {
					setDiggerTile(nextState, CELL_DOOR);
					nextState.availableKeys--;
				}
				break;
			case ACTION_EXIT:
				setDiggerTile(nextState, CELL_EXIT);
				nextState.hasExit = true;
				break;
			default:
				throw new RuntimeException("Unknown action: " + action);
		}

		return nextState;
	}

	/**
	 * Modifies state s to be the result of the digger moving a single space
	 * in a cardinal direction.
	 *
	 * @param nextState
	 * @param action
	 */
	private void move(DungeonState s, String action) {
		int newX = s.x;
		int newY = s.y;

		if(action == ACTION_UP)
			newY++;
		else if (action == ACTION_DOWN)
			newY--;
		else if(action == ACTION_LEFT)
			newX--;
		else if(action == ACTION_RIGHT)
			newX++;

		// Moves outside of the bounds of the level are invalid and result in no effect
		if(newX < 0 || newX >= s.getLevelWidth() || newY < 0 || newY >= s.getLevelHeight())
			return;

		// Update the agents location
		s.x = newX;
		s.y = newY;

		// If the new location is a block, dig it out
		if(s.level[newX][newY] == CELL_BLOCK) {
			int[][] newLevel = deepCopyLevel(s);
			newLevel[newX][newY] = CELL_OPEN;
			s.level = newLevel;
		}
	}

	/**
	 * Alters the state s in response to a room being created centered
	 * around the digger agent.
	 *
	 * @param s
	 * @param roomSize
	 */
	private void createRoom(DungeonState s, int roomSize) {
		int halfRoomSize = roomSize / 2;
		int left = (int)Math.floor(s.x - halfRoomSize);
		int right = (int)Math.floor(s.x + halfRoomSize);
		int top = (int)Math.floor(s.y + halfRoomSize);
		int bottom = (int)Math.floor(s.y - halfRoomSize);

		if(left < 0)
			left = 0;
		if(right >= s.getLevelWidth())
			right = s.getLevelWidth() - 1;
		if(bottom < 0)
			bottom = 0;
		if(top >= s.getLevelHeight())
			top = s.getLevelHeight() - 1;

		int[][] levelCopy = deepCopyLevel(s);

		for(int x = left; x <= right; x++)
			for(int y = bottom; y <= top; y++)
				setTileAt(levelCopy, x, y, CELL_OPEN);

		s.level = levelCopy;
	}

	/**
	 * Alters the state s to set a tile in the level at the current
	 * location of the digger.
	 *
	 * @param s
	 * @param tile
	 */
	private void setDiggerTile(DungeonState s, int tile) {
		int[][] levelCopy = deepCopyLevel(s);
		setTileAt(levelCopy, s.x, s.y, tile);
		s.level = levelCopy;
	}

	/**
	 * Sets a tile at a specific location in the given level.
	 *
	 * Doesn't allow certain tiles to be overwritten.
	 *
	 * @param level
	 * @param x
	 * @param y
	 * @param tile
	 */
	private void setTileAt(int[][] level, int x, int y, int tile) {
		if(level[x][y] == CELL_KEY)
			return;
		level[x][y] = tile;
	}

	/**
	 * Makes a deep copy of the level in a state
	 *
	 * @param s The state from which the level should be copied
	 * @return A deep copy of the int[][] for the level
	 */
	private int[][] deepCopyLevel(DungeonState s) {
		int[][] copy = new int[s.getLevelWidth()][s.getLevelHeight()];
		for(int i = 0; i < s.getLevelWidth(); i++)
			for(int j = 0; j < s.getLevelHeight(); j++)
				copy[i][j] = s.level[i][j];
		return copy;
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		return FullStateModel.Helper.deterministicTransition(this, s, a);
	}


}
