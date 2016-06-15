package vfeqs.experiment.strategy.ranking.scorer;

import vfeqs.model.RORRanking;

public class ExpectedChangeScoreMerger implements ScoreMerger {
    @Override
    public Double merge(RORRanking ranking, Scorer scorer, int a, int b, Double aOverBScore, Double bOverAScore) {
        double abPWI = ranking.getPWI(a, b);
        double baPWI = ranking.getPWI(b, a);

        if (abPWI + baPWI == 0.0) {
            return 0.0;
        } else {
            return (abPWI * aOverBScore + baPWI * bOverAScore) / (abPWI + baPWI);
        }
    }

    @Override
    public String toString() {
        return "ExpectedChange";
    }
}
