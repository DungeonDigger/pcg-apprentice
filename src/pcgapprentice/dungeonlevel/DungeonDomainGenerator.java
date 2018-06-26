package pcgapprentice.dungeonlevel;

import java.util.HashMap;
import java.util.Map;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;
import pcgapprentice.dungeonlevel.visualization.AgentPainter;
import pcgapprentice.dungeonlevel.visualization.LevelPainter;

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

	// Limited state variables
	public static final String VAR_VISION = "vision";
	public static final String VAR_ENEMY_COUNT = "enemy-count";
	public static final String VAR_TREASURE_COUNT = "treasure-count";
	public static final String VAR_DOOR_COUNT = "door-count";
	public static final String VAR_OPEN_COUNT = "open-count";
	public static final String VAR_AVAILABLE_KEYS = "available-keys";
	public static final String VAR_HAS_EXIT = "has-exit";
	public static final String VAR_ROOM_WOULD_INTERSECT = "intersect";
	public static final String VAR_SENSOR_NORTH = "north";
	public static final String VAR_SENSOR_SOUTH = "south";
	public static final String VAR_SENSOR_EAST = "east";
	public static final String VAR_SENSOR_WEST = "west";
	public static final String VAR_DISTANCE = "start-distance";


	// Tilemap cell values
	public static final int CELL_VOID = -1;
	public static final int CELL_BLOCK = 0;
	public static final int CELL_OPEN = 1;
	public static final int CELL_TREASURE = 2;
	public static final int CELL_ENEMY = 3;
	public static final int CELL_EXIT = 4;
	public static final int CELL_KEY = 5;
	public static final int CELL_DOOR = 6;

	Map<String, HashMap<String, HashMap<String, Double>>> transitionProbabilities;
	int visionRadius;

	public DungeonDomainGenerator(Map<String, HashMap<String, HashMap<String, Double>>> transitionProbabilities,
								  int visionRadius) {
		super();
		this.transitionProbabilities = transitionProbabilities;
		this.visionRadius = visionRadius;
	}

	@Override
	public SADomain generateDomain() {
		SADomain domain = new SADomain();

		domain.addActionTypes(
				new ObservedActionType(ACTION_UP, transitionProbabilities),
				new ObservedActionType(ACTION_DOWN, transitionProbabilities),
				new ObservedActionType(ACTION_LEFT, transitionProbabilities),
				new ObservedActionType(ACTION_RIGHT, transitionProbabilities),
				new ObservedActionType(ACTION_ROOM_SMALL, transitionProbabilities),
				new ObservedActionType(ACTION_ROOM_MED, transitionProbabilities),
				new ObservedActionType(ACTION_ROOM_LARGE, transitionProbabilities),
				new ObservedActionType(ACTION_TREASURE, transitionProbabilities),
				new ObservedActionType(ACTION_ENEMY, transitionProbabilities),
				new ObservedActionType(ACTION_KEY, transitionProbabilities),
				new ObservedActionType(ACTION_DOOR, transitionProbabilities),
				new ObservedActionType(ACTION_EXIT, transitionProbabilities));

		DungeonLimitedStateModel stateModel = new DungeonLimitedStateModel(transitionProbabilities, visionRadius);
		// The reward function doesn't really matter here - the goal is to learn a real one!
		RewardFunction rf = new UniformCostRF();
		TerminalFunction tf = new DungeonTF();

		domain.setModel(new FactoredModel(stateModel, rf, tf));

		return domain;
	}

	/**
	 * Gets a visualizer that can render the full state of the dungeon generation
	 * world.
	 *
	 * @return A visualizer for DungeonStates
	 */
	public static Visualizer getFullStateVisualizer() {
		StateRenderLayer rl = new StateRenderLayer();
		rl.addStatePainter(new LevelPainter());
		rl.addStatePainter(new AgentPainter());
		return new Visualizer(rl);
	}

}
