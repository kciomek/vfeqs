package vfeqs.experiment.strategy.ranking.scorer;

import vfeqs.model.RORRanking;

public class ERAScorer implements Scorer {
    @Override
    public Double score(RORRanking parentRanking, RORRanking ranking) {
        return parentRanking.getERA() - ranking.getERA();
    }

    @Override
    public boolean isBetter(Double score, Double bestScore) {
        return bestScore == null || score > bestScore;
    }

    @Override
    public String toString() {
        return "ERA";
    }
}
