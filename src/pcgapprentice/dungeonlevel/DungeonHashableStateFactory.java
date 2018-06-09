package pcgapprentice.dungeonlevel;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

public class DungeonHashableStateFactory implements HashableStateFactory {

	@Override
	public HashableState hashState(State s) {
		return new HashableDungeonLimitedState(s);
	}

}
