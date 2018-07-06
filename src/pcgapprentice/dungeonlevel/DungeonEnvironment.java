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
	private int visionRadius;
	private boolean useFullState;

	public DungeonEnvironment(RewardFunction rf, int visionRadius, boolean useFullState) {
		this.rf = rf;
		this.visionRadius = visionRadius;
		this.resetEnvironment();
		fullModel = new DungeonStateModel();
		this.useFullState = useFullState;
	}

	@Override
	public State currentObservation() {
		if(useFullState)
			return fullHiddenState;
		// Get the limited state view
		return new DungeonLimitedState(fullHiddenState.x, fullHiddenState.y, fullHiddenState.level,
				fullHiddenState.availableKeys, fullHiddenState.hasExit, visionRadius);
	}

	@Override
	public EnvironmentOutcome executeAction(Action a) {
		// Get the next "full" state
		DungeonState nextState = (DungeonState)(fullModel.sample(fullHiddenState, a));

		// Find the next limited view state
		DungeonLimitedState currentState = new DungeonLimitedState(fullHiddenState.x, fullHiddenState.y, fullHiddenState.level,
				fullHiddenState.availableKeys, fullHiddenState.hasExit, visionRadius);
		DungeonLimitedState nextLimitedState = new DungeonLimitedState(nextState.x, nextState.y, nextState.level,
				nextState.availableKeys, nextState.hasExit, visionRadius);

		// Prepare the outcome
		double reward = rf.reward(currentState, a, nextLimitedState);
		EnvironmentOutcome outcome;
		if(useFullState) {
			outcome = new EnvironmentOutcome(fullHiddenState, a, nextState, reward, nextState.hasExit);
		} else {
			outcome = new EnvironmentOutcome(currentState, a, nextLimitedState, reward, nextLimitedState.hasExit);
		}

		// Update the current view of the world
		fullHiddenState = nextState;
		lastReward = reward;

		return outcome;
	}

	@Override
	public double lastReward() {
		return lastReward;
	}

	@Override
	public boolean isInTerminalState() {
		return fullHiddenState.hasExit;
	}

	@Override
	public void resetEnvironment() {
		int[][] level = new int[100][100];
		level[49][50] = 1;
		fullHiddenState = new DungeonState(49, 50, level, 0, false);
		lastReward = 0;
	}

}
