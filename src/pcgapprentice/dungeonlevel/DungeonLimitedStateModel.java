package pcgapprentice.dungeonlevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

public class DungeonLimitedStateModel implements FullStateModel {

	Map<String, HashMap<String, HashMap<String, Double>>> transitionProbs;

	public DungeonLimitedStateModel(Map<String, HashMap<String, HashMap<String, Double>>> transitionProbs) {
		super();
		this.transitionProbs = transitionProbs;
	}

	@Override
	public State sample(State s, Action a) {
		List<StateTransitionProb> transitionProbs = stateTransitions(s, a);

		// Use a random roll to sample
		double r = Math.random();
		double sumProb = 0.;
		for (StateTransitionProb stateTransitionProb : transitionProbs) {
			sumProb += stateTransitionProb.p;
			if(r < sumProb) {
				System.out.println("S': " + ((DungeonLimitedState)stateTransitionProb.s).toString());
				return stateTransitionProb.s;
			}
		}

		// Should be unreachable
		System.out.println("Undefined state transition!");
		return s;
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		DungeonLimitedState ds = (DungeonLimitedState)s;
		List<StateTransitionProb> transitions = new ArrayList<StateTransitionProb>();

		if(!transitionProbs.containsKey(ds.toString())) {
			System.out.println("butts");
		}

		// This action has not been defined for this state.
		// Remain in the same state with 100% probability
		if(!transitionProbs.get(ds.toString()).containsKey(a.actionName())) {
			transitions.add(new StateTransitionProb(s, 1));
			return transitions;
		}

		// The action is defined for this state. Extract the list of transition probabilities
		for (Entry<String, Double> e: transitionProbs.get(ds.toString()).get(a.actionName()).entrySet()) {
			transitions.add(new StateTransitionProb(new DungeonLimitedState(e.getKey()), e.getValue()));
		}
		return transitions;
	}

}
