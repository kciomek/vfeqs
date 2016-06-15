package vfeqs.model.solution;

import vfeqs.model.VFProblem;

public class VFRankingSolution extends VFSolution {
    private final double accuracy;
    private int[] rank;

    public VFRankingSolution(double[] variableValues, VFProblem problem, double accuracy) {
        super(variableValues, problem);

        this.accuracy = accuracy;
        this.rank = null;
    }

    public boolean isWeaklyPreferred(int alternativeAIndex, int alternativeBIndex) {
        return this.getValue(alternativeAIndex) - this.getValue(alternativeBIndex) >= -this.accuracy;
    }

    public boolean isStrictlyPreferred(int alternativeAIndex, int alternativeBIndex) {
        return this.getValue(alternativeAIndex) - this.getValue(alternativeBIndex) >= this.accuracy;
    }

    public int getRank(int alternativeIndex) {
        if (this.rank == null) {
            this.rank = new int[this.problem.getNumberOfAlternatives()];

            for (int k = 0; k < this.problem.getNumberOfAlternatives(); k++) {
                int better = 0;

                for (int i = 0; i < this.problem.getNumberOfAlternatives(); i++) {
                    if (i != k) {
                        if (this.isStrictlyPreferred(i, k)) {
                            better++;
                        }
                    }
                }

                this.rank[k] = better + 1;
            }
        }

        return this.rank[alternativeIndex];
    }
}
