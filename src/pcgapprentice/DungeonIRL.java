package pcgapprentice;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearning;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearningRequest;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.singleagent.SADomain;
import pcgapprentice.dungeonlevel.DungeonDomainGenerator;
import pcgapprentice.dungeonlevel.DungeonFeatures;
import pcgapprentice.dungeonlevel.DungeonHashableStateFactory;
import pcgapprentice.dungeonlevel.DungeonStartStateGenerator;
import pcgapprentice.dungeonlevel.utils.EpisodeReader;

public class DungeonIRL {

	public static void main(String[] args) {
		try {
			Map<String, HashMap<String, HashMap<String, Double>>> traj = EpisodeReader.readTrajectoriesFromFile(
					new String[] {"data/20180605221946-full-demo.dat"});

			// Create a new domain generator whose MDP will be created from our sampled trajectories
			DungeonDomainGenerator dungeonDomain = new DungeonDomainGenerator(traj);
			SADomain domain = dungeonDomain.generateDomain();

			// Load the episodes to train the IRL with
			Episode test = EpisodeReader.readDatasetFromFile("data/20180605221946-full-demo.dat");
			ArrayList<Episode> expertEpisodes = new ArrayList<Episode>();
			expertEpisodes.add(test);

			// Use simple ValueIteration as a planner
			ValueIteration planner = new ValueIteration(domain, 0.99, new DungeonHashableStateFactory(), 0.001, 100);

			DungeonFeatures features = new DungeonFeatures();

			StateGenerator startStateGenerator = new DungeonStartStateGenerator();

			// Construct the apprenticeship learning request and produce a policy
			ApprenticeshipLearningRequest request = new ApprenticeshipLearningRequest(domain, planner, features, expertEpisodes, startStateGenerator);
			Policy learnedPolicy = ApprenticeshipLearning.getLearnedPolicy(request);

			System.out.println("Finished building policy");

			// Write the policy to a file
			Date dNow = new Date();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
			planner.writeValueTable("data/out/" + ft.format(dNow) + "_valtable.yml");

			// Write the rollout to a file
			Episode sampleRollout = PolicyUtils.rollout(learnedPolicy, startStateGenerator.generateState(), domain.getModel(), 200);
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
