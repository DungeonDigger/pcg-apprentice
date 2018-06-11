package pcgapprentice.dungeonlevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;

public class EmptyCurrentTileActionType implements ActionType {
	public String typeName;
	public Action action;
	protected List<Action> allActions;

	public EmptyCurrentTileActionType(String typeName) {
		this.typeName = typeName;
		this.action = new SimpleAction(typeName);
		this.allActions = Arrays.asList(this.action);
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
		DungeonLimitedState ds = (DungeonLimitedState)s;
		if(ds.vision[1][1] != DungeonDomainGenerator.CELL_OPEN)
			return new ArrayList<Action>();

		return allActions;
	}
}
