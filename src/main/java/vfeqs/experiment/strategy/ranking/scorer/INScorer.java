package vfeqs.experiment.strategy.ranking.scorer;

import vfeqs.model.RORRanking;

public class INScorer implements Scorer {

    @Override
    public Double score(RORRanking parentRanking, RORRanking ranking) {
        return (double) (ranking.getNumberOfNecessaryRelations() - parentRanking.getNumberOfNecessaryRelations());
    }

    @Override
    public boolean isBetter(Double score, Double bestScore) {
        return bestScore == null || score > bestScore;
    }

    @Override
    public String toString() {
        return "IN";
    }
}
