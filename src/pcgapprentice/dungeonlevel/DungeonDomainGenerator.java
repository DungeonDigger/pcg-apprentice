package pcgapprentice.dungeonlevel;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;

public class DungeonDomainGenerator implements DomainGenerator {

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

	public static final String VAR_X = "x";
	public static final String VAR_Y = "y";
	public static final String VAR_LEVEL = "level";

	@Override
	public Domain generateDomain() {
		// TODO Auto-generated method stub
		return null;
	}

}
