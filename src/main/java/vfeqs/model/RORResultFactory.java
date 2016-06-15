package vfeqs.model;

import polyrun.thinning.ThinningFunction;

import java.util.Map;

public interface RORResultFactory {
    RORResult create(VFProblem problem, int numberOfSamples, double epsilon,
                     ThinningFunction thinningFunction, boolean calculateProbability, Map parameters);
}
