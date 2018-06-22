package pcgapprentice.dungeonlevel;

import java.util.Arrays;
import java.util.Objects;

import burlap.mdp.core.state.State;
import burlap.statehashing.WrappedHashableState;

public class HashableDungeonLimitedState extends WrappedHashableState {

	public HashableDungeonLimitedState(State s) {
		super(s);
	}

	@Override
	public int hashCode() {
		DungeonLimitedState ds = (DungeonLimitedState)s;
		final int prime = 31;
		int result = 1;
		result = prime * result + ds.availableKeys;
		result = prime * result + ds.doorCount;
		result = prime * result + ds.enemyCount;
		result = prime * result + (ds.hasExit ? 1231 : 1237);
		result = prime * result + ds.openCount;
		result = prime * result + (ds.roomWouldIntersect ? 1231 : 1237);
		result = prime * result + ds.treasureCount;
		result = prime * result + Arrays.deepHashCode(ds.vision);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		DungeonLimitedState ds = (DungeonLimitedState)s;
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashableDungeonLimitedState o = (HashableDungeonLimitedState) obj;
		DungeonLimitedState other = (DungeonLimitedState)o.s;
		if (ds.availableKeys != other.availableKeys)
			return false;
		if (ds.doorCount != other.doorCount)
			return false;
		if (ds.enemyCount != other.enemyCount)
			return false;
		if (ds.hasExit != other.hasExit)
			return false;
		if (ds.openCount != other.openCount)
			return false;
		if (ds.treasureCount != other.treasureCount)
			return false;
		if (ds.roomWouldIntersect != other.roomWouldIntersect)
			return false;
		if (!Arrays.deepEquals(ds.vision, other.vision))
			return false;
		return true;
	}

}
