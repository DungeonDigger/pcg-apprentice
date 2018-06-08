package pcgapprentice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearning;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearningRequest;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import pcgapprentice.dungeonlevel.DungeonDomainGenerator;
import pcgapprentice.dungeonlevel.DungeonFeatures;
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
//
			// Load the episodes to train the IRL with
			Episode test = EpisodeReader.readDatasetFromFile("data/20180605221946-full-demo.dat");
			ArrayList<Episode> expertEpisodes = new ArrayList<Episode>();
			expertEpisodes.add(test);

			// Use simple ValueIteration as a planner
			ValueIteration planner = new ValueIteration(domain, 0.99, new SimpleHashableStateFactory(), 0.001, 100);
//
			DungeonFeatures features = new DungeonFeatures();
//			LinearStateDifferentiableRF rf = new LinearStateDifferentiableRF(features, 1);
//			for(int i = 0; i < rf.numParameters(); i++) {
//				rf.setParameter(i, RandomFactory.getMapped(0).nextDouble() * 0.2 - 0.1);
//			}
//
//			double beta = 10;
//			DifferentiableSparseSampling dplanner = new DifferentiableSparseSampling(domain, rf, 0.99,
//					new SimpleHashableStateFactory(), 10, -1, beta);
//

//
//			MLIRLRequest irlRequest = new MLIRLRequest(domain, dplanner, expertEpisodes, rf);
//
//			// Run MLIRL
//			MLIRL irl = new MLIRL(irlRequest, 0.1, 0.1, 100);
//			irl.performIRL();
//
//			Policy policy = new GreedyQPolicy((QProvider) irlRequest.getPlanner());

//			QLearning planner = new QLearning(domain, 0.99, new SimpleHashableStateFactory(), 0., 1.);
//			planner.initializeForPlanning(10);
//			Planner planner = new ValueIteration(domain, 0.99, new SimpleHashableStateFactory(), 0.001, 100);
//
//
			StateGenerator startStateGenerator = new DungeonStartStateGenerator();
//
			ApprenticeshipLearningRequest request = new ApprenticeshipLearningRequest(domain, planner, features, expertEpisodes, startStateGenerator);
//
			Policy learnedPolicy = ApprenticeshipLearning.getLearnedPolicy(request);
//
//			DungeonLimitedStateModel stateModel = new DungeonLimitedStateModel(traj);
//			// The reward function doesn't really matter here - the goal is to learn a real one!
//			RewardFunction rf = new UniformCostRF();
//			TerminalFunction tf = new DungeonTF();
//
			Episode giveitago = PolicyUtils.rollout(learnedPolicy, startStateGenerator.generateState(), domain.getModel());

			System.out.println("Done!");
		} catch (IOException e) {
			System.err.println("Unable to read episode file: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
