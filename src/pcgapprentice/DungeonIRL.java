package pcgapprentice;

import java.io.IOException;
import java.util.ArrayList;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearning;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearningRequest;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import pcgapprentice.dungeonlevel.DungeonDomainGenerator;
import pcgapprentice.dungeonlevel.DungeonFeatures;
import pcgapprentice.dungeonlevel.DungeonStartStateGenerator;
import pcgapprentice.dungeonlevel.DungeonStateModel;
import pcgapprentice.dungeonlevel.DungeonTF;
import pcgapprentice.dungeonlevel.utils.EpisodeReader;

public class DungeonIRL {

	public static void main(String[] args) {
		try {
			Episode test = EpisodeReader.readDatasetFromFile("data/20180605221946-full-demo.dat");

			SADomain domain = new DungeonDomainGenerator().generateDomain();
			QLearning planner = new QLearning(domain, 0.9, new SimpleHashableStateFactory(), 0., 1.);
			planner.initializeForPlanning(10);
			DenseStateFeatures features = new DungeonFeatures();
			ArrayList<Episode> expertEpisodes = new ArrayList<Episode>();
			expertEpisodes.add(test);
			StateGenerator startStateGenerator = new DungeonStartStateGenerator();

			ApprenticeshipLearningRequest request = new ApprenticeshipLearningRequest(domain, planner, features, expertEpisodes, startStateGenerator);

			Policy learnedPolicy = ApprenticeshipLearning.getLearnedPolicy(request);

			DungeonStateModel stateModel = new DungeonStateModel();
			// The reward function doesn't really matter here - the goal is to learn a real one!
			RewardFunction rf = new UniformCostRF();
			TerminalFunction tf = new DungeonTF();

			Episode giveitago = PolicyUtils.rollout(learnedPolicy, startStateGenerator.generateState(), new FactoredModel(stateModel, rf, tf));

			System.out.println("Done!");
		} catch (IOException e) {
			System.err.println("Unable to read episode file: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
