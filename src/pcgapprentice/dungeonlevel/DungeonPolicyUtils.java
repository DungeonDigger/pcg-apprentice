package pcgapprentice.dungeonlevel;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.SampleModel;

import java.util.List;
import java.util.Optional;
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
        SameVisionState,
        None
    }

    public enum RolloutType {
        Normal,
        Smart
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
     * @param rolloutType The type of rollout to perform
     * @return An episode obtained from following the policy
     */
    public static Episode rolloutWithRefreshProbability(Policy p, State s, SampleModel model, Set<String> stateStrings,
                                                        int visionRadius, int maxSteps, double refreshProbIncr,
                                                        int maxRefreshes, RolloutRefreshType refreshType,
                                                        RolloutType rolloutType) {
        SimulatedEnvironment env = new SimulatedEnvironment(model, s);
        DungeonEnvironment fullEnv = new DungeonEnvironment(new UniformCostRF(), 1, true);
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

                if(rolloutType == RolloutType.Smart) {
                    // Make sure this action isn't stupid
                    DungeonState fullCurrentState = (DungeonState)fullEnv.currentObservation();
                    DungeonLimitedState realCurrentState = new DungeonLimitedState(fullCurrentState.x, fullCurrentState.y,
                            fullCurrentState.level, fullCurrentState.availableKeys, fullCurrentState.hasExit, visionRadius);
                    List<ActionProb> probs = ((GreedyQPolicy)p).policyDistribution(env.currentObservation());
                    // Make sure there are other options to choose from if the selected one is stupid
                    if(probs.size() > 1) {
                        if((a.actionName() == DungeonDomainGenerator.ACTION_ROOM_LARGE && realCurrentState.roomWouldIntersect)
                                || (a.actionName() == DungeonDomainGenerator.ACTION_UP && realCurrentState.sensorNorth)
                                || (a.actionName() == DungeonDomainGenerator.ACTION_DOWN && realCurrentState.sensorSouth)
                                || (a.actionName() == DungeonDomainGenerator.ACTION_RIGHT && realCurrentState.sensorEast)
                                || (a.actionName() == DungeonDomainGenerator.ACTION_LEFT && realCurrentState.sensorWest)) {
                            String actionName = a.actionName();
                            Optional<ActionProb> dumb = probs.stream()
                                    .filter(actionProb -> actionProb.ga.actionName() == actionName).findFirst();
                            if(dumb.isPresent()) {
                                probs.remove(dumb.get());
                            }

                            a = probs.get(rand.nextInt(probs.size())).ga;
                        }
                    }
                }

                EnvironmentOutcome outcome = env.executeAction(a);
                ep.transition(a, outcome.op, outcome.r);
                fullEnv.executeAction(a);

                numSteps++;

                // Increment the chance of refreshing to the start state
                refresh += refreshProbIncr;
            }
        } while(!env.isInTerminalState() && numSteps < maxSteps);

        return ep;
    }
}
