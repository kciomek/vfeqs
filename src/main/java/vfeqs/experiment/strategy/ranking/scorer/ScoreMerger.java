package vfeqs.experiment.strategy.ranking.scorer;

import vfeqs.model.RORRanking;

public interface ScoreMerger {
    Double merge(RORRanking ranking, Scorer scorer, int a, int b, Double aOverBScore, Double bOverAScore);
}
