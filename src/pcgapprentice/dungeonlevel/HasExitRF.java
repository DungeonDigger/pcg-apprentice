package pcgapprentice.dungeonlevel;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

/**
 * A custom reward function for DungeonLimitedStates to apply
 * a reward when the state contains an exit tile and zero otherwise.
 */
public class HasExitRF implements RewardFunction {

	private double exitReward;

	public HasExitRF(double exitReward) {
		this.exitReward = exitReward;
	}

	@Override
	public double reward(State s, Action a, State sprime) {
		DungeonLimitedState ds = (DungeonLimitedState)sprime;
		if(ds.hasExit) {
			return exitReward;
		}
		return 0;
	}

}
