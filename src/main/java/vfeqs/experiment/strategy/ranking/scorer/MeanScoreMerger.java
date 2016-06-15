package vfeqs.experiment.strategy.ranking.scorer;

import vfeqs.model.RORRanking;

public class MeanScoreMerger implements ScoreMerger {
    @Override
    public Double merge(RORRanking ranking, Scorer scorer, int a, int b, Double aOverBScore, Double bOverAScore) {
        return (aOverBScore + bOverAScore) / 2.0;
    }

    @Override
    public String toString() {
        return "MeanMerger";
    }
}
