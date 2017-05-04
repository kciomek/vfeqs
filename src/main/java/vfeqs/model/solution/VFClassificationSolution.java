package vfeqs.model.solution;

import vfeqs.model.VFProblem;

public class VFClassificationSolution extends VFSolution {
//    private final double accuracy;
//    private final double[] thresholds;
    private final Integer[] assignments;

    public VFClassificationSolution(double[] variableValues, VFProblem problem, double accuracy) {
        super(variableValues, problem);

//        this.accuracy = accuracy;
        this.assignments = new Integer[problem.getNumberOfAlternatives()];

        double[] thresholds;

        if (problem.getNumberOfClasses() > 0) {
            thresholds = new double[problem.getNumberOfClasses()];

            if (problem.getThresholds() == null) {
                System.arraycopy(variableValues, problem.getFirstThresholdIndex(), thresholds, 0, problem.getNumberOfClasses() - 1);
            } else {
                thresholds = problem.getThresholds();
            }
        } else {
            throw new RuntimeException("Cannot built VFClassificationSolution with no classes.");
        }

        for (int alternative = 0; alternative < problem.getNumberOfAlternatives(); alternative++) {
            this.assignments[alternative] = 0;

            for (int i = 0; i < problem.getNumberOfClasses() - 1; i++) {
                if (getValue(alternative) < thresholds[i] - accuracy) {
                    break;
                }

                this.assignments[alternative]++;
            }
        }

        this.values = null;
    }

//    public double[] getThresholds() {
//        return thresholds;
//    }

    public synchronized int getAssignment(int alternative) {
        return this.assignments[alternative];
    }
}
