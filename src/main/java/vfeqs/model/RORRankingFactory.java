package vfeqs.model;

import polyrun.thinning.ThinningFunction;

import java.util.Map;

public class RORRankingFactory implements RORResultFactory {
    @Override
    public RORResult create(VFProblem problem, int numberOfSamples, double epsilon,
                            ThinningFunction thinningFunction, boolean calculateProbability, Map parameters) {
        return new RORRanking(problem, numberOfSamples, epsilon, thinningFunction, calculateProbability, parameters);
    }

    @Override
    public String toString() {
        return "R";
    }
}
