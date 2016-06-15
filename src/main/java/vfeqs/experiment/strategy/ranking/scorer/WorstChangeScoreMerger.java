package vfeqs.experiment.strategy.ranking.scorer;

import vfeqs.model.RORRanking;

public class WorstChangeScoreMerger implements ScoreMerger {
    @Override
    public Double merge(RORRanking ranking, Scorer scorer, int a, int b, Double aOverBScore, Double bOverAScore) {
        return scorer.isBetter(aOverBScore, bOverAScore) ? bOverAScore : aOverBScore;
    }

    @Override
    public String toString() {
        return "WorstChange";
    }
}
