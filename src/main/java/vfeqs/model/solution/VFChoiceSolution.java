package vfeqs.model.solution;

import vfeqs.model.VFProblem;

public class VFChoiceSolution extends VFSolution {
    private Integer best;

    public VFChoiceSolution(double[] variableValues, VFProblem problem) {
        super(variableValues, problem);
    }

    public int getBest() {
        if (this.best == null) {
            double bestValue = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < this.problem.getNumberOfAlternatives(); i++) {
                double value = this.getValue(i);

                if (value > bestValue) {
                    bestValue = value;
                    this.best = i;
                }
            }
        }

        return this.best;
    }

    public boolean isWeaklyPreferred(int alternativeAIndex, int alternativeBIndex) {
        return this.getValue(alternativeAIndex) >= this.getValue(alternativeBIndex);
    }
}
