package pcgapprentice.dungeonlevel;

/**
 * Encapsulates a request to create a rollout of a dungeon policy.
 */
public class DungeonRolloutRequest {
    public int maxSteps;
    public double refreshProbIncr;
    public int maxRefreshes;
    public DungeonPolicyUtils.RolloutRefreshType refreshType;

    public DungeonRolloutRequest(int maxSteps, double refreshProbIncr, int maxRefreshes,
                                 DungeonPolicyUtils.RolloutRefreshType refreshType) {
        this.maxSteps = maxSteps;
        this.refreshProbIncr = refreshProbIncr;
        this.maxRefreshes = maxRefreshes;
        this.refreshType = refreshType;
    }
}
