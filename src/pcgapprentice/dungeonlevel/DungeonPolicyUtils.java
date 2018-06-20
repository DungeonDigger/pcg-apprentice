package pcgapprentice.dungeonlevel;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.SampleModel;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A set of static utility methods for interacting with policies generated
 * for the Dungeon level generator domain.
 */
public class DungeonPolicyUtils {

    public enum RolloutRefreshType {
        StartState,
        RandomState,
        SameVisionState
    }

    /**
     * Follows the policy from the given start state, terminating either when it reaches
     * a terminal state or hits the maximum number of time steps. At each time step,
     * the probability of resetting the agent's view back to the start state increases
     * by the provided increment.
     *
     * @param p The policy to roll out
     * @param s The start state
     * @param model The model to sample from
     * @param visionRadius The radius of the agent's vision
     * @param stateStrings The list of all valid states in the MDP in string form
     * @param maxSteps The maximum number of steps to take for the policy
     * @param refreshProbIncr The amount by which the probability of restarting to the start
     *                        state increases after each step
     * @param maxRefreshes The maximum number of times the agent will reset its state during
     *                     the run
     * @param refreshType The type of state refresh to utilize
     * @return An episode obtained from following the policy
     */
    public static Episode rolloutWithRefreshProbability(Policy p, State s, SampleModel model, Set<String> stateStrings,
                                                        int visionRadius, int maxSteps, double refreshProbIncr,
                                                        int maxRefreshes, RolloutRefreshType refreshType) {
        SimulatedEnvironment env = new SimulatedEnvironment(model, s);
        Episode ep = new Episode(env.currentObservation());
        double refresh = 0;
        int refreshCount = 0;
        Random rand = new Random();

        int numSteps = 0;
        do {
            // Make a random roll to see if we should refresh to the starting state
            double r = Math.random();
            if(refresh > r && refreshCount < maxRefreshes) {
                DungeonLimitedState currentState = (DungeonLimitedState)env.currentObservation();
                State refreshState = currentState;
                switch(refreshType) {
                    case StartState:
                        refreshState = s;
                        break;
                    case RandomState:
                        int selection = rand.nextInt(stateStrings.size());
                        int i = 0;
                        for(String stateString : stateStrings) {
                            if(i == selection) {
                                refreshState = new DungeonLimitedState(stateString, visionRadius);
                                break;
                            }
                            i++;
                        }
                        break;
                    case SameVisionState:
                        // Try to find a state with the same agent field of vision
                        String currentVision = currentState.getVisionString();
                        List<String> validStates = stateStrings.stream().
                                filter(s1 -> s1.startsWith(currentVision) && !s1.equals(currentState.toString())).collect(Collectors.toList());
                        if(validStates.size() > 0) {
                            // Select one at random to transition to
                            int select = rand.nextInt(validStates.size());
                            refreshState = new DungeonLimitedState(validStates.get(select), visionRadius);
                        }
                        break;
                }

                // Return to the chosen state and continue evaluating from there
                env = new SimulatedEnvironment(model, refreshState);
                refresh = 0;
                refreshCount++;
            } else {
                // Take an action according to the policy
                Action a = p.action(env.currentObservation());
                EnvironmentOutcome outcome = env.executeAction(a);
                ep.transition(a, outcome.op, outcome.r);

                numSteps++;

                // Increment the chance of refreshing to the start state
                refresh += refreshProbIncr;
            }
        } while(!env.isInTerminalState() && numSteps < maxSteps);

        return ep;
    }
}
