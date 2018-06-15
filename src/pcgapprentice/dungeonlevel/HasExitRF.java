package pcgapprentice.dungeonlevel;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class HasExitRF implements RewardFunction {

	@Override
	public double reward(State s, Action a, State sprime) {
		DungeonLimitedState ds = (DungeonLimitedState)sprime;
		if(ds.hasExit) {
			return 10;
		}
		return 0;
	}

}
