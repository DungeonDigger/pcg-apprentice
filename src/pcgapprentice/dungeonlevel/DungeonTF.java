package pcgapprentice.dungeonlevel;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

/**
 * A terminal function for the dungeon building agent. Returns
 * true when there is an exit tile in the level.
 *
 * @author evanc
 */
public class DungeonTF implements TerminalFunction {

	@Override
	public boolean isTerminal(State s) {
		DungeonLimitedState ds = (DungeonLimitedState)s;
		return ds.hasExit;
	}

}
