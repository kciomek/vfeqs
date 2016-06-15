package vfeqs.experiment.strategy.ranking;

import vfeqs.experiment.strategy.RankingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORRanking;
import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.model.RORResult;

import java.util.ArrayList;

public class DVFStrategy extends RankingStrategy {
    public DVFStrategy() {
        super(null);
    }

    @Override
    public StrategyResult chooseQuestion(RORRanking rorRanking) {
        // assuming that there is at least one pair to compare
        PairwiseComparisonQuestion theChosenOne = null;
        double maxScore = -1.0;

        for (PairwiseComparisonQuestion pair : rorRanking.getQuestions()) {
            double score = Math.min(rorRanking.getPWI(pair.getFirst(), pair.getSecond()), rorRanking.getPWI(pair.getSecond(), pair.getFirst()));

            if (theChosenOne == null || score > maxScore) {
                maxScore = score;
                theChosenOne = pair;
            }
        }

        return new StrategyResult(theChosenOne, new ArrayList<RORResult>());
    }

    @Override
    public String toString() {
        return "DVF";
    }
}
