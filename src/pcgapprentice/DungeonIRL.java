package pcgapprentice;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearningRequest;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import pcgapprentice.dungeonlevel.DungeonDomainGenerator;
import pcgapprentice.dungeonlevel.DungeonFeatures;
import pcgapprentice.dungeonlevel.DungeonHashableStateFactory;
import pcgapprentice.dungeonlevel.DungeonLimitedStateModel;
import pcgapprentice.dungeonlevel.DungeonStartStateGenerator;
import pcgapprentice.dungeonlevel.DungeonTF;
import pcgapprentice.dungeonlevel.HasExitRF;
import pcgapprentice.dungeonlevel.utils.DemonstrationData;
import pcgapprentice.dungeonlevel.utils.EpisodeReader;
import pcgapprentice.reward.AggregatedRF;

public class DungeonIRL {

	public static void main(String[] args) {
		try {
			String[] demoFiles = new String[] {"data/20180605221946-full-demo.dat",
					"data/20180610131233-full-demo.dat",
					"data/20180610131426-full-demo.dat"};
			String[] enemyDemoFiles = new String[] {"data/enemy-demo (1).dat",
					"data/enemy-demo (2).dat",
					"data/enemy-demo (3).dat"};
			// Load the episodes to train the IRL with
			List<Episode> expertEpisodes = EpisodeReader.readEpisodesFromFiles(
					enemyDemoFiles);
			DemonstrationData demoData = EpisodeReader.getDemonstrationDataFromEpisodes(
					expertEpisodes);
			Map<String, HashMap<String, HashMap<String, Double>>> freq = demoData.frequencyData;

			// Create a new domain generator whose MDP will be created from our sampled trajectories
			DungeonDomainGenerator dungeonDomain = new DungeonDomainGenerator(freq);
			SADomain domain = dungeonDomain.generateDomain();



			// Use simple ValueIteration as a planner
			ValueIteration planner = new ValueIteration(domain, 0.99, new DungeonHashableStateFactory(), 0.001, 100);

			DungeonFeatures features = new DungeonFeatures(demoData.maxEnemies, demoData.maxTreasures, demoData.maxDoors, demoData.maxOpen);

			StateGenerator startStateGenerator = new DungeonStartStateGenerator();

			// Construct the apprenticeship learning request and produce a policy
			ApprenticeshipLearningRequest request = new ApprenticeshipLearningRequest(domain, planner, features, expertEpisodes, startStateGenerator);
			request.setEpsilon(0.0001);
//			Policy learnedPolicy = ApprenticeshipLearning.getLearnedPolicy(request);
			RewardFunction learnedReward = ApprenticeshipLearning2.projectionMethodReward(request);
			
			RewardFunction combinedReward = new AggregatedRF(Arrays.asList(learnedReward, new HasExitRF()));
			DungeonLimitedStateModel stateModel = new DungeonLimitedStateModel(freq);
			// The reward function doesn't really matter here - the goal is to learn a real one!
			TerminalFunction tf = new DungeonTF();

			domain.setModel(new FactoredModel(stateModel, combinedReward, tf));
			planner.setDomain(domain);

			planner.resetSolver();
			GreedyQPolicy superPolicy = planner.planFromState(startStateGenerator.generateState());
			
			Episode sampleRollout = PolicyUtils.rollout(superPolicy, startStateGenerator.generateState(), domain.getModel(), 200);
			

			// Create an environment for Q-learning
//			Environment dungeonEnvironment = new DungeonEnvironment(learnedReward);
//			LearningAgent agent = new QLearning(domain, 0.99, new DungeonHashableStateFactory(), 100., 0.1);
//
//			Episode e = null;
//			for(int i = 0; i < 1000; i++) {
//				try {
//					e = agent.runLearningEpisode(dungeonEnvironment);
//				} catch(RuntimeException ex) {}
//
//
//				dungeonEnvironment.resetEnvironment();
//			}



			System.out.println("Finished building policy");

			// Write the policy to a file
			Date dNow = new Date();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
			planner.writeValueTable("data/out/" + ft.format(dNow) + "_valtable.yml");

			// Write the rollout to a file
//			Episode sampleRollout = PolicyUtils.rollout(learnedPolicy, startStateGenerator.generateState(), domain.getModel(), 200);
			String actionRollout = sampleRollout.actionSequence.stream().map(n -> n.actionName()).collect(Collectors.joining("\n"));
			PrintWriter out = new PrintWriter("data/out/" + ft.format(dNow) + "_rollout.txt");
			out.println(actionRollout);
			out.close();

			System.out.println("Done!");
		} catch (IOException e) {
			System.err.println("Unable to read episode file: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
