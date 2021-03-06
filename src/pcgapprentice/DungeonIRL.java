package pcgapprentice;

import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearningRequest;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.visualizer.Visualizer;
import pcgapprentice.dungeonlevel.*;
import pcgapprentice.dungeonlevel.utils.DemonstrationData;
import pcgapprentice.dungeonlevel.utils.EpisodeReader;
import pcgapprentice.dungeonlevel.utils.LevelCleanup;
import pcgapprentice.dungeonlevel.utils.LevelMetrics;
import pcgapprentice.reward.AggregatedRF;

/**
 * The main entry point for running apprenticeship learning and generating policies.
 * Please refer to `trainIrlAgentAndGenerateEpisode` for details on parameters that
 * can be used to customize the training.
 */
public class DungeonIRL {

	public static void main(String[] args) {
		try {
			String[] demoFiles = new String[] {"data/20180605221946-full-demo.dat",
					"data/20180610131233-full-demo.dat",
					"data/20180610131426-full-demo.dat"};
			String[] enemyDemoFiles = new String[] {"data/enemy-demo (1).dat",
					"data/enemy-demo (2).dat",
					"data/enemy-demo (3).dat"};
			String[] zeldaFiles = new String[] {
					"data/zelda-1-full-demo.dat",
					"data/zelda-2-full-demo.dat",
					"data/zelda-3-full-demo.dat",
					"data/zelda-4-full-demo.dat",
					"data/zelda-5-full-demo.dat",
					"data/zelda-6-full-demo.dat",
					"data/zelda-7-full-demo.dat",
					"data/zelda-8-full-demo.dat"
			};

			String[] allFiles = new String[] {
//					"data/20180605221946-full-demo.dat",
//					"data/20180610131233-full-demo.dat",
//					"data/20180610131426-full-demo.dat",
//					"data/enemy-demo (1).dat",
//					"data/enemy-demo (2).dat",
//					"data/enemy-demo (3).dat",
					"data/alex-demo (1).dat",
					"data/alex-demo (2).dat",
					"data/alex-demo (3).dat",
					"data/alex-demo (4).dat",
					"data/alex-demo (5).dat",
					"data/evan-demo (1).dat",
					"data/evan-demo (2).dat",
					"data/evan-demo (3).dat",
					"data/evan-demo (4).dat",
					"data/evan-demo (5).dat"};

			List<Episode> eps = trainIrlAgentAndGenerateEpisode(zeldaFiles, 10000, 3,
					new DungeonRolloutRequest[] {
							new DungeonRolloutRequest(400, 0.01, 0,
									DungeonPolicyUtils.RolloutRefreshType.None,
									DungeonPolicyUtils.RolloutType.Dumb),
							new DungeonRolloutRequest(400, 0.01, 0,
									DungeonPolicyUtils.RolloutRefreshType.None,
									DungeonPolicyUtils.RolloutType.Normal),
							new DungeonRolloutRequest(400, 0.01, 0,
									DungeonPolicyUtils.RolloutRefreshType.None,
									DungeonPolicyUtils.RolloutType.Smart),
							new DungeonRolloutRequest(400, 0.005, 3,
									DungeonPolicyUtils.RolloutRefreshType.StartState,
									DungeonPolicyUtils.RolloutType.Normal),
							new DungeonRolloutRequest(400, 0.005, 3,
									DungeonPolicyUtils.RolloutRefreshType.RandomState,
									DungeonPolicyUtils.RolloutType.Normal),
							new DungeonRolloutRequest(400, 0.005, 3,
									DungeonPolicyUtils.RolloutRefreshType.SameVisionState,
									DungeonPolicyUtils.RolloutType.Normal)
					});

			// Uncomment this block to generate episodes from a saved policy rather
			// than train from scratch.
//			String policyFile = "data/savedagentlevels/2018_10_04_120750_valtable.yml";
//			List<Episode> eps = generateEpisodesFromSavedPolicy(zeldaFiles, 3,
//					new DungeonRolloutRequest[] {
//							new DungeonRolloutRequest(400, 0.01, 0,
//									DungeonPolicyUtils.RolloutRefreshType.None,
//									DungeonPolicyUtils.RolloutType.Dumb),
//							new DungeonRolloutRequest(400, 0.01, 0,
//									DungeonPolicyUtils.RolloutRefreshType.None,
//									DungeonPolicyUtils.RolloutType.Normal),
//							new DungeonRolloutRequest(400, 0.01, 0,
//									DungeonPolicyUtils.RolloutRefreshType.None,
//									DungeonPolicyUtils.RolloutType.Smart),
//							new DungeonRolloutRequest(400, 0.005, 3,
//									DungeonPolicyUtils.RolloutRefreshType.StartState,
//									DungeonPolicyUtils.RolloutType.Normal),
//							new DungeonRolloutRequest(400, 0.005, 3,
//									DungeonPolicyUtils.RolloutRefreshType.RandomState,
//									DungeonPolicyUtils.RolloutType.Normal),
//							new DungeonRolloutRequest(400, 0.005, 3,
//									DungeonPolicyUtils.RolloutRefreshType.SameVisionState,
//									DungeonPolicyUtils.RolloutType.Normal)
//					}, policyFile);

			int levelNum = 1;
			for(Episode ep : eps) {
				String levelName = "Level " + levelNum;
				Date dNow = new Date();
				SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
				String levelOutFile = "data/out/" + ft.format(dNow) + "_level_" + levelNum + ".txt";
				int[][] level = buildLevel(ep, levelName);
				writeLevelToFile(level, levelOutFile);
				System.out.println(levelName + " Stats");
				System.out.println("------------------");
				double exploration = LevelMetrics.getExplorationPercentage(level, 49, 50);
				double linearity = LevelMetrics.getLinearity(level,49, 50);
				double leniency = LevelMetrics.getLeniency(level, 49, 50);
				double density = LevelMetrics.getDensity(level);
				System.out.println("Exploration: " + exploration);
				System.out.println("Linearity: " + linearity);
				System.out.println("Leniency: " + leniency);
				System.out.println("Density: " + density);
				System.out.println();
				levelNum++;
			}

			System.out.println("Done!");
		} catch (IOException e) {
			System.err.println("Unable to read episode file: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Runs apprenticeship learning with the given set of expert demonstration files and trains an agent
	 * based on the learned reward function. Returns a list of episodes generated by the policy of this agent.
	 *
	 * @param expertDemonstrationFiles The files to read in constructing the MDP and expert trajectories
	 * @param endReward An additional reward to be granted to the agent for reaching the end state
	 * @param agentVisionRadius The radius of vision that the agent has when constructing the MDP. e.g. a value
	 *                          of 3 means the agent will have a 3x3 square centered around itself as a part of
	 *                          its state.
	 * @param rolloutRequests Requests to produce various rollouts of the learned policy
	 * @return A list of episodes generated by the learned agent.
	 * @throws IOException
	 */
	private static List<Episode> trainIrlAgentAndGenerateEpisode(String[] expertDemonstrationFiles, double endReward,
														   int agentVisionRadius,
														   DungeonRolloutRequest[] rolloutRequests) throws IOException {
		// Load the episodes to train the IRL with
		List<Episode> expertEpisodes = EpisodeReader.readEpisodesFromFiles(
				expertDemonstrationFiles, agentVisionRadius);
		// Extract data from the episodes such as frequency data of state transitions
		DemonstrationData demoData = EpisodeReader.getDemonstrationDataFromEpisodes(
				expertEpisodes);
		Map<String, HashMap<String, HashMap<String, Double>>> freq = demoData.frequencyData;

		// Create a new domain generator whose MDP will be created from our sampled frequency data
		DungeonDomainGenerator dungeonDomain = new DungeonDomainGenerator(freq, agentVisionRadius);
		SADomain domain = dungeonDomain.generateDomain();

		// Use simple ValueIteration as a planner for solving MDPs during apprenticeship learning
		ValueIteration planner = new ValueIteration(domain, 0.99, new DungeonHashableStateFactory(), 0.001, 100);

		// Create the class for extracting feature vectors from states
		DungeonFeatures features = new DungeonFeatures(demoData.maxEnemies, demoData.maxTreasures, demoData.maxDoors, demoData.maxOpen);

		// Create a generator for the initial state in the MDP
		StateGenerator startStateGenerator = new DungeonStartStateGenerator(agentVisionRadius);

		// Construct the apprenticeship learning request and produce a policy
		ApprenticeshipLearningRequest request = new ApprenticeshipLearningRequest(domain, planner, features, expertEpisodes, startStateGenerator);
		RewardFunction learnedReward = ApprenticeshipLearning2.projectionMethodReward(request);

		// Construct a new reward function which combines the learned one with a custom reward for reaching the "end" state
		RewardFunction combinedReward = new AggregatedRF(Arrays.asList(learnedReward, new HasExitRF(endReward)));

		// Solve the MDP using the combined reward function
		domain.setModel(new FactoredModel(new DungeonLimitedStateModel(freq, agentVisionRadius), combinedReward, new DungeonTF()));
		planner.setDomain(domain);
		planner.setGamma(0.95);
		planner.resetSolver();
		GreedyQPolicy policy = planner.planFromState(startStateGenerator.generateState());
		System.out.println("Finished building policy");

		// Write the policy to a file
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
		String policyOutFile = "data/out/" + ft.format(dNow) + "_valtable.yml";
		planner.writeValueTable(policyOutFile);
		System.out.println("Policy value table written to: " + policyOutFile);

		List<Episode> episodes = new ArrayList<>();
		int epNum = 1;
		for (DungeonRolloutRequest rolloutRequest : rolloutRequests) {
			// Rollout the learned policy to get a sequence of states and actions
			Episode agentEp = DungeonPolicyUtils.rolloutWithRefreshProbability(policy,
					startStateGenerator.generateState(), domain.getModel(), freq.keySet(), agentVisionRadius,
					rolloutRequest.maxSteps, rolloutRequest.refreshProbIncr, rolloutRequest.maxRefreshes,
					rolloutRequest.refreshType, rolloutRequest.rolloutType);
			episodes.add(agentEp);
			// Write the rollout to a file
			String actionRollout = agentEp.actionSequence.stream().map(n -> n.actionName()).collect(Collectors.joining("\n"));
			String rolloutFilePath = "data/out/" + ft.format(dNow) + "_rollout_" + (epNum++) + ".txt";
			PrintWriter out = new PrintWriter(rolloutFilePath);
			out.println(actionRollout);
			out.close();
			System.out.println("Rollout written to: " + rolloutFilePath);
		}

		return episodes;
	}

	/**
	 * Builds a domain with the given set of expert demonstrations and loads a saved policy file
	 * (i.e. planner value table). Returns a list of policies generated by an agent with this policy.
	 *
	 * @param expertDemonstrationFiles The files to read in constructing the MDP and expert trajectories
	 * @param agentVisionRadius The radius of vision that the agent has when constructing the MDP. e.g. a value
	 *                          of 3 means the agent will have a 3x3 square centered around itself as a part of
	 *                          its state.
	 * @param rolloutRequests Requests to produce various rollouts of the learned policy
	 * @param valueTablePath The path to the file where the value table of a policy's planner is written.
	 * @return A list of episodes generated by the learned agent.
	 * @throws IOException
	 */
	private static List<Episode> generateEpisodesFromSavedPolicy(String[] expertDemonstrationFiles, int agentVisionRadius,
																 DungeonRolloutRequest[] rolloutRequests,
																 String valueTablePath)
			throws IOException{
		// Load the episodes to construct the domain
		List<Episode> expertEpisodes = EpisodeReader.readEpisodesFromFiles(
				expertDemonstrationFiles, agentVisionRadius);
		// Extract data from the episodes such as frequency data of state transitions
		DemonstrationData demoData = EpisodeReader.getDemonstrationDataFromEpisodes(
				expertEpisodes);
		Map<String, HashMap<String, HashMap<String, Double>>> freq = demoData.frequencyData;

		// Create a generator for the initial state in the MDP
		StateGenerator startStateGenerator = new DungeonStartStateGenerator(agentVisionRadius);

		// Create a new domain generator whose MDP will be created from our sampled frequency data
		DungeonDomainGenerator dungeonDomain = new DungeonDomainGenerator(freq, agentVisionRadius);
		SADomain domain = dungeonDomain.generateDomain();

		// Create a new planner and load its value table from disk
		ValueIteration planner = new ValueIteration(domain, 0.99, new DungeonHashableStateFactory(), 0.001, 100);
		planner.loadValueTable(valueTablePath);

		// Create a policy from the loaded planner
		GreedyQPolicy policy = new GreedyQPolicy(planner);

		List<Episode> episodes = new ArrayList<>();
		int epNum = 1;
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
		for (DungeonRolloutRequest rolloutRequest : rolloutRequests) {
			// Rollout the learned policy to get a sequence of states and actions
			Episode agentEp = DungeonPolicyUtils.rolloutWithRefreshProbability(policy,
					startStateGenerator.generateState(), domain.getModel(), freq.keySet(), agentVisionRadius,
					rolloutRequest.maxSteps, rolloutRequest.refreshProbIncr, rolloutRequest.maxRefreshes,
					rolloutRequest.refreshType, rolloutRequest.rolloutType);
			episodes.add(agentEp);
			// Write the rollout to a file
			String actionRollout = agentEp.actionSequence.stream().map(n -> n.actionName()).collect(Collectors.joining("\n"));
			String rolloutFilePath = "data/out/" + ft.format(dNow) + "_rollout_" + (epNum++) + ".txt";
			PrintWriter out = new PrintWriter(rolloutFilePath);
			out.println(actionRollout);
			out.close();
			System.out.println("Rollout written to: " + rolloutFilePath);
		}

		return episodes;
	}

	/**
	 * Renders an episode of an agent into a panel for a quick visualization of
	 * the agent's run and the final generated level.
	 *
	 * @param ep The episode to visualize
	 */
	private static int[][] buildLevel(Episode ep, String name) {
		// Initialize an environment and the visualizer
		DungeonEnvironment environment = new DungeonEnvironment(new UniformCostRF(), 1, true);
		Visualizer v = DungeonDomainGenerator.getFullStateVisualizer();
		v.setPreferredSize(new Dimension(1200, 1200));
		VisualActionObserver observer = new VisualActionObserver(v);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(v);
		frame.pack();
		frame.setVisible(true);
		frame.setTitle(name);

		v.updateState(environment.currentObservation());

		// Iterate through each action in the episode and update the state
		for(Action a : ep.actionSequence) {
			environment.executeAction(a);
			v.updateState(environment.currentObservation());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}

		DungeonState ds = (DungeonState)environment.currentObservation();
		int[][] cleanedLevel = LevelCleanup.getCleanedLevel(ds.level);
		v.updateState(new DungeonState(ds.x, ds.y, cleanedLevel, ds.availableKeys, ds.hasExit));
		return cleanedLevel;
	}

	/**
	 * Writes a level matrix to a file.
	 *
	 * @param level The 2D array representing the level
	 * @param destFile The destination file to write to
	 * @throws IOException
	 */
	private static void writeLevelToFile(int[][] level, String destFile) throws IOException {
		int iMax = level.length;
		int jMax = level[0].length;
		List<String> rows = new ArrayList<>();

		for(int j = jMax - 1; j >= 0; j--) {
			String row = "";
			for(int i = 0; i < iMax; i++) {
				row += level[i][j] + " ";
			}
			rows.add(row);
		}

		String levelString = String.join("\n", rows);
		PrintWriter out = new PrintWriter(destFile);
		out.println(levelString);
		out.close();
		System.out.println("Level written to: " + destFile);
	}

}
