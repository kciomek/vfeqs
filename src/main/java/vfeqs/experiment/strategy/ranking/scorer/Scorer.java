package vfeqs.experiment.strategy.ranking.scorer;

import vfeqs.model.RORRanking;

public interface Scorer {
    Double score(RORRanking parentRanking, RORRanking ranking);

    boolean isBetter(Double score, Double bestScore);
}
