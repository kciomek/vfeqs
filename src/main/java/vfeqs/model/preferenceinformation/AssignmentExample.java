package vfeqs.model.preferenceinformation;

import vfeqs.model.Constraint;
import vfeqs.model.VFProblem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AssignmentExample implements PreferenceInformation {
    public enum Type {
        AT_LEAST(">"),
        AT_MOST("<"),
        EXACT("=");

        private final String stringValue;

        Type(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    private final int alternative;
    private final int classIndex;
    private final Type type;

    public AssignmentExample(int alternative, Type type, int classIndex) {
        this.alternative = alternative;
        this.classIndex = classIndex;
        this.type = type;
    }

    @Override
    public Collection<Constraint> getConstraints(VFProblem problem, Double epsilon) {
        List<Constraint> lst = new ArrayList<Constraint>();

        if (classIndex > 0 && (this.type == Type.AT_LEAST || this.type == Type.EXACT)) {
            lst.add(AssignmentExample.buildConstraint(problem, alternative, true, classIndex, epsilon));
        }


        if (classIndex < problem.getNumberOfClasses() - 1 && (this.type == Type.AT_MOST || this.type == Type.EXACT)) {
            lst.add(AssignmentExample.buildConstraint(problem, alternative, false, classIndex, epsilon));
        }

        return lst;
    }

    public int getAlternative() {
        return alternative;
    }

    public Integer getClassIndex() {
        return classIndex;
    }

    private static Constraint buildConstraint(VFProblem problem, int alternative, boolean atLeast, int classIndex, Double epsilon) {
        if (classIndex == 0 && atLeast) {
            return null;
        } else if (classIndex == problem.getNumberOfClasses() - 1 && !atLeast) {
            return null;
        }

        final Map<Integer, Double>[][] valuesToVariables = problem.getValuesToVariables();
        final int numberOfSegments = problem.getNumberOfCharacteristicPoints() - 1;
        int numberOfVariables = numberOfSegments * problem.getNumberOfCriteria();

        if (epsilon == null) {
            numberOfVariables += 1;
        }

        if (problem.getNumberOfClasses() > 1 && problem.getThresholds() == null) {
            numberOfVariables += problem.getNumberOfClasses() - 1;
        }

        double[] lhs = new double[numberOfVariables];
        double rhs = 0.0;

        for (int i = 0; i < problem.getNumberOfCriteria(); i++) {
            for (Map.Entry<Integer, Double> entry : valuesToVariables[alternative][i].entrySet()) {
                lhs[numberOfSegments * i + entry.getKey() - 1] += entry.getValue();
            }
        }

        String direction;

        if (problem.getThresholds() == null) {
            if (atLeast) {
                lhs[problem.getFirstThresholdIndex() + classIndex - 1] = -1.0;
                direction = ">=";
            } else {
                lhs[problem.getFirstThresholdIndex() + classIndex] = -1.0;

                direction = "<=";

                if (epsilon == null) {
                    lhs[lhs.length - 1] = 1.0;
                } else {
                    rhs = -epsilon;
                }
            }
        } else {
            if (atLeast) {
                direction = ">=";
                rhs = problem.getThresholds()[classIndex - 1];
            } else {
                direction = "<=";
                rhs = problem.getThresholds()[classIndex];

                if (epsilon == null) {
                    lhs[lhs.length - 1] = 1.0;
                } else {
                    rhs -= epsilon;
                }
            }
        }

        return new Constraint(lhs, direction, rhs, "");
    }
}
