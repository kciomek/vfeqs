package vfeqs.experiment.strategy.ranking;

import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.strategy.RankingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORRanking;
import vfeqs.model.RORResult;
import vfeqs.model.VFModel;
import vfeqs.model.preferenceinformation.PreferenceComparison;

import java.util.ArrayList;

public class SQEvalStrategy extends RankingStrategy {
    public SQEvalStrategy() {
        super(null);
    }

    @Override
    public StrategyResult chooseQuestion(RORRanking rorRanking) {
        // assuming that there is at least one pair to compare

        PairwiseComparisonQuestion theChosenOne = null;
        double maxScore = Double.POSITIVE_INFINITY;

        for (PairwiseComparisonQuestion pair : rorRanking.getQuestions()) {
            VFModel modelAOverB = new VFModel(rorRanking.getModel().getConstraints());
            VFModel modelBOverA = new VFModel(rorRanking.getModel().getConstraints());

            modelAOverB.addAll(new PreferenceComparison(pair.getFirst(), pair.getSecond()).getConstraints(rorRanking.getProblem(), rorRanking.getEpsilon()));
            modelBOverA.addAll(new PreferenceComparison(pair.getSecond(), pair.getFirst()).getConstraints(rorRanking.getProblem(), rorRanking.getEpsilon()));

            double score = Math.abs(modelAOverB.getBoundingBoxPseudoHyperVolume() - modelBOverA.getBoundingBoxPseudoHyperVolume());

            if (theChosenOne == null || score > maxScore) {
                maxScore = score;
                theChosenOne = pair;
            }
        }

        return new StrategyResult(theChosenOne, new ArrayList<RORResult>());
    }

    @Override
    public String toString() {
        return "SQEVAL";
    }
}
