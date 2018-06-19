package pcgapprentice.dungeonlevel;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.SampleModel;

public class DungeonPolicyUtils {

    /**
     * Follows the policy from the given start state, terminating either when it reaches
     * a terminal state or hits the maximum number of time steps. At each time step,
     * the probability of resetting the agent's view back to the start state increases
     * by the provided increment.
     *
     * @param p The policy to roll out
     * @param s The start state
     * @param model The model to sample from
     * @param maxSteps The maximum number of steps to take for the policy
     * @param refreshProbIncr The amount by which the probability of restarting to the start
     *                        state increases after each step
     * @param maxRefreshes The maximum number of times the agent will reset its state during
     *                     the run
     * @return An episode obtained from following the policy
     */
    public static Episode rolloutWithRefreshProbability(Policy p, State s, SampleModel model, int maxSteps,
                                                        double refreshProbIncr, int maxRefreshes) {
        SimulatedEnvironment env = new SimulatedEnvironment(model, s);
        Episode ep = new Episode(env.currentObservation());
        double refresh = 0;
        int refreshCount = 0;

        int numSteps = 0;
        do {
            // Make a random roll to see if we should refresh to the starting state
            double r = Math.random();
            if(refresh > r && refreshCount < maxRefreshes) {
                // Return to the start state
                env = new SimulatedEnvironment(model, s);
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
