package pcgapprentice.dungeonlevel;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;

/**
 * A generator for the start state of the MDP. The initial
 * state is an empty level with the agent starting at
 * x=24,y=0
 */
public class DungeonStartStateGenerator implements StateGenerator {

	private int visionRadius;

	public DungeonStartStateGenerator(int visionRadius) {
		this.visionRadius = visionRadius;
	}

	@Override
	public State generateState() {
		int[][] level = new int[50][50];
		level[24][0] = 1;
		DungeonLimitedState ds = new DungeonLimitedState(24, 0, level, 0, false, visionRadius);
		// Normalize to 0 to account for the discretization that happens when these are read
		ds.openCount = 0;
		return ds;
	}

}
