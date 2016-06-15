package vfeqs.model.solution;

import vfeqs.model.VFProblem;

import java.util.Map;

public abstract class VFSolution {
    protected double[] values;
    protected final VFProblem problem;

    public VFSolution(double[] variableValues, VFProblem problem) {
        this.problem = problem;
        this.values = new double[problem.getNumberOfAlternatives()];

        for (int i = 0; i < problem.getNumberOfAlternatives(); i++) {
            double value = 0.0;

            for (int j = 0; j < problem.getNumberOfCriteria(); j++) {
                for (Map.Entry<Integer, Double> entry : this.problem.getValuesToVariables()[i][j].entrySet()) {
                    value += variableValues[(problem.getNumberOfCharacteristicPoints() - 1) * j + entry.getKey() - 1] * entry.getValue();
                }
            }

            this.values[i] = value;
        }
    }

    public double getValue(int alternativeIndex) {
        return this.values[alternativeIndex];
    }
}
