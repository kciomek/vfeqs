package vfeqs.optimization;

import vfeqs.model.VFModel;

public interface GLPVariableOptimizer {
    enum Direction {
        Minimize,
        Maximize
    }

    OptimizationResult optimize(Direction direction, int variableIndex, VFModel constraints) throws UnboundedSystemException, InfeasibleSystemException;
}
