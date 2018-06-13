package pcgapprentice.dungeonlevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;

/**
 * An ActionType for simple, unparameterized actions where the actions
 * available from a given state are restricted to only those that have
 * been observed from a set of training data.
 *
 * In this way, the agent is prevented from entering unknown state space.
 *
 * @author evanc
 *
 */
public class ObservedActionType implements ActionType {

	public String typeName;
	public Action action;
	protected Map<String, Set<String>> availableActions;
	protected List<Action> allActions;

	public ObservedActionType(String typeName, Map<String, HashMap<String, HashMap<String, Double>>> frequencyData) {
		this.typeName = typeName;
		this.action = new SimpleAction(typeName);
		this.allActions = Arrays.asList(this.action);
		this.availableActions = new HashMap<String, Set<String>>();

		for (Entry<String, HashMap<String, HashMap<String, Double>>> entry : frequencyData.entrySet()) {
			this.availableActions.put(entry.getKey(), entry.getValue().keySet());
		}
	}

	@Override
	public String typeName() {
		return typeName;
	}

	@Override
	public Action associatedAction(String strRep) {
		return action;
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		// Check whether or not this action is valid from the given state
		DungeonLimitedState ds = (DungeonLimitedState)s;
		if(!availableActions.containsKey(ds.toString()) && action.actionName() == "PlaceExit") {
			return allActions;
		}

		if(!availableActions.containsKey(ds.toString()) || !availableActions.get(ds.toString()).contains(typeName))
			return new ArrayList<Action>();

		return allActions;
	}

}
