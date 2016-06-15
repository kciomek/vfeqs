package vfeqs.experiment.strategy;

import vfeqs.model.RORRanking;

import java.util.Map;

public abstract class RankingStrategy extends Strategy<RORRanking> {
    protected RankingStrategy(Map resultParameters) {
        super(resultParameters);
    }
}