package pcgapprentice.dungeonlevel;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import burlap.mdp.core.state.annotations.ShallowCopyState;

/**
 * A representation of the world state for the dungeon-level
 * digger agent.
 *
 * @author evanc
 */
@ShallowCopyState
public class DungeonState implements MutableState {

	public int x;
	public int y;
	public int[][] level;

	private final static List<Object> keys = Arrays.<Object>asList(
		DungeonDomainGenerator.VAR_X,
		DungeonDomainGenerator.VAR_Y,
		DungeonDomainGenerator.VAR_LEVEL
	);

	public DungeonState() {}

	public DungeonState(int x, int y, int[][] level) {
		super();
		this.x = x;
		this.y = y;
		this.level = level;
	}

	@Override
	public State copy() {
		return new DungeonState(x, y, level);
	}

	@Override
	public Object get(Object variableKey) {
		switch(variableKey.toString()) {
			case DungeonDomainGenerator.VAR_X:
				return x;
			case DungeonDomainGenerator.VAR_Y:
				return y;
			case DungeonDomainGenerator.VAR_LEVEL:
				return level;
			default:
				throw new UnknownKeyException(variableKey);
		}
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		switch(variableKey.toString()) {
			case DungeonDomainGenerator.VAR_X:
				x = StateUtilities.stringOrNumber(value).intValue();
				break;
			case DungeonDomainGenerator.VAR_Y:
				y = StateUtilities.stringOrNumber(value).intValue();
				break;
			case DungeonDomainGenerator.VAR_LEVEL:
				this.level = (int[][])value;
				break;
			default:
				throw new UnknownKeyException(variableKey);
		}
		return this;
	}

	@Override
	public String toString() {
		return StateUtilities.stateToString(this);
	}

}
