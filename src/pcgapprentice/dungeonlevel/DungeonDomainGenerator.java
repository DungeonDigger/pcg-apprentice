package pcgapprentice.dungeonlevel;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;

public class DungeonDomainGenerator implements DomainGenerator {

	// Actions
	public static final String ACTION_UP = "Up";
	public static final String ACTION_DOWN = "Down";
	public static final String ACTION_LEFT = "Left";
	public static final String ACTION_RIGHT = "Right";

	public static final String ACTION_ROOM_SMALL = "RoomSmall";
	public static final String ACTION_ROOM_MED = "RoomMedium";
	public static final String ACTION_ROOM_LARGE = "RoomLarge";

	public static final String ACTION_TREASURE = "PlaceTreasure";
	public static final String ACTION_ENEMY = "PlaceEnemy";
	public static final String ACTION_KEY = "PlaceKey";
	public static final String ACTION_DOOR = "PlaceDoor";
	public static final String ACTION_EXIT = "PlaceExit";

	// State variables
	public static final String VAR_X = "x";
	public static final String VAR_Y = "y";
	public static final String VAR_LEVEL = "level";
	public static final String VAR_AVAILABLE_KEYS = "available-keys";
	public static final String VAR_HAS_EXIT = "has-exit";

	// Tilemap cell values
	public static final int CELL_BLOCK = 0;
	public static final int CELL_OPEN = 1;
	public static final int CELL_TREASURE = 2;
	public static final int CELL_ENEMY = 3;
	public static final int CELL_EXIT = 4;
	public static final int CELL_KEY = 5;
	public static final int CELL_DOOR = 6;

	@Override
	public Domain generateDomain() {
		SADomain domain = new SADomain();

		domain.addActionTypes(
				new UniversalActionType(ACTION_UP),
				new UniversalActionType(ACTION_DOWN),
				new UniversalActionType(ACTION_LEFT),
				new UniversalActionType(ACTION_RIGHT),
				new UniversalActionType(ACTION_ROOM_SMALL),
				new UniversalActionType(ACTION_ROOM_MED),
				new UniversalActionType(ACTION_ROOM_LARGE),
				new UniversalActionType(ACTION_TREASURE),
				new UniversalActionType(ACTION_ENEMY),
				new UniversalActionType(ACTION_KEY),
				new UniversalActionType(ACTION_DOOR),
				new UniversalActionType(ACTION_EXIT));

		DungeonStateModel stateModel = new DungeonStateModel();
		// The reward function doesn't really matter here - the goal is to learn a real one!
		RewardFunction rf = new UniformCostRF();
		TerminalFunction tf = new DungeonTF();

		domain.setModel(new FactoredModel(stateModel, rf, tf));

		return domain;
	}

}
