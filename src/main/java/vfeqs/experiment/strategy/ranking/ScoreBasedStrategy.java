package vfeqs.experiment.strategy.ranking;

import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.strategy.RankingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORResult;
import vfeqs.experiment.strategy.ranking.scorer.Scorer;
import vfeqs.experiment.strategy.ranking.scorer.ScoreMerger;
import vfeqs.model.RORRanking;

import java.util.ArrayList;
import java.util.List;

public class ScoreBasedStrategy extends RankingStrategy {
    private final Scorer scorer;
    private final ScoreMerger merger;

    public ScoreBasedStrategy(Scorer scorer, ScoreMerger merger) {
        super(null);

        this.scorer = scorer;
        this.merger = merger;
    }

    @Override
    public StrategyResult chooseQuestion(RORRanking rorRanking) {
        // assuming that there is at least one pair to compare
        StrategyResult theChosenOne = null;
        Double bestScore = null;

        for (PairwiseComparisonQuestion pair : rorRanking.getQuestions()) {
            RORRanking aOverBRanking = (RORRanking) rorRanking.createSuccessor(pair, 0);
            RORRanking bOverARanking = (RORRanking) rorRanking.createSuccessor(pair, 1);

            Double score = merger.merge(rorRanking, this.scorer, pair.getFirst(), pair.getSecond(),
                    scorer.score(rorRanking, aOverBRanking), scorer.score(rorRanking, bOverARanking));

            if (scorer.isBetter(score, bestScore)) {
                bestScore = score;
                List<RORResult> lst = new ArrayList<RORResult>();
                lst.add(aOverBRanking);
                lst.add(bOverARanking);
                theChosenOne = new StrategyResult(pair, lst);
            }
        }

        return theChosenOne;
    }

    @Override
    public String toString() {
        return this.merger.toString().substring(0, 1) + "-" + this.scorer.toString();
    }
}
