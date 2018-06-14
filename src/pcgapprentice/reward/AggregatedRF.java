package pcgapprentice.reward;

import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class AggregatedRF implements RewardFunction {

	private List<RewardFunction> rfs;
	
	public AggregatedRF(List<RewardFunction> rfs) {
		super();
		this.rfs = rfs;
	}

	@Override
	public double reward(State s, Action a, State sprime) {
		double reward = 0.;
		for (RewardFunction rewardFunction : rfs) {
			reward += rewardFunction.reward(s, a, sprime);
		}
		return reward;
	}

}
