package vfeqs.experiment.strategy.ranking.scorer;

import vfeqs.model.RORRanking;

public class PEScorer implements Scorer {

    @Override
    public Double score(RORRanking parentRanking, RORRanking ranking) {
        return parentRanking.getPE() - ranking.getPE();
    }

    @Override
    public boolean isBetter(Double score, Double bestScore) {
        return bestScore == null || score > bestScore;
    }

    @Override
    public String toString() {
        return "PE";
    }
}
