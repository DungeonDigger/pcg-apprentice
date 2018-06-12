package pcgapprentice.dungeonlevel;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.RewardFunction;

public class DungeonEnvironment implements Environment {

	private DungeonState fullHiddenState;
	private DungeonStateModel fullModel;
	private RewardFunction rf;
	private double lastReward = 0.;

	public DungeonEnvironment(RewardFunction rf) {
		this.rf = rf;
		this.resetEnvironment();
		fullModel = new DungeonStateModel();
	}

	@Override
	public State currentObservation() {
		// Get the limited state view
		return new DungeonLimitedState(fullHiddenState.x, fullHiddenState.y, fullHiddenState.level,
				fullHiddenState.availableKeys, fullHiddenState.hasExit);
	}

	@Override
	public EnvironmentOutcome executeAction(Action a) {
		// Get the next "full" state
		DungeonState nextState = (DungeonState)(fullModel.sample(fullHiddenState, a));

		// Find the next limited view state
		DungeonLimitedState currentState = (DungeonLimitedState) currentObservation();
		DungeonLimitedState nextLimitedState = new DungeonLimitedState(nextState.x, nextState.y, nextState.level,
				nextState.availableKeys, nextState.hasExit);

		// Prepare the outcome
		double reward = rf.reward(currentState, a, nextLimitedState);
		EnvironmentOutcome outcome = new EnvironmentOutcome(currentState, a, nextLimitedState, reward, nextLimitedState.hasExit);

		// Update the current view of the world
		fullHiddenState = nextState;
		lastReward = reward;

		return outcome;
	}

	@Override
	public double lastReward() {
		return lastReward();
	}

	@Override
	public boolean isInTerminalState() {
		return fullHiddenState.hasExit;
	}

	@Override
	public void resetEnvironment() {
		int[][] level = new int[50][50];
		level[24][0] = 1;
		fullHiddenState = new DungeonState(24, 0, level, 0, false);
		lastReward = 0;
	}

}
