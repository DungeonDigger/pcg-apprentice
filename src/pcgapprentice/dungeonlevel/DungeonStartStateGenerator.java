package pcgapprentice.dungeonlevel;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;

public class DungeonStartStateGenerator implements StateGenerator {

	@Override
	public State generateState() {
		int[][] level = new int[50][50];
		level[24][0] = 1;
		DungeonLimitedState ds = new DungeonLimitedState(24, 0, level, 0, false);
		// Normalize to 0 to account for the discretization that happens when these are read
		ds.openCount = 0;
		return ds;
	}

}
