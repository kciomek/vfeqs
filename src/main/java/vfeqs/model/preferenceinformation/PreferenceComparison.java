package vfeqs.model.preferenceinformation;

import vfeqs.model.Constraint;
import vfeqs.model.VFProblem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PreferenceComparison implements PreferenceInformation {
    public enum Type {
        Indifference("="),
        StrictPreference(">"),
        WeakPreference(">=");

        private final String stringValue;

        Type(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    private final int first;
    private final int second;
    private final Type type;

    public PreferenceComparison(int first, int second) {
        this(first, second, Type.StrictPreference);
    }

    public PreferenceComparison(int first, int second, Type type) {
        this.first = first;
        this.second = second;
        this.type = type;
    }

    @Override
    public Collection<Constraint> getConstraints(VFProblem problem, Double epsilon) {
        List<Constraint> lst = new ArrayList<Constraint>();

        final Map<Integer, Double>[][] valuesToVariables = problem.getValuesToVariables();
        final int numberOfSegments = problem.getNumberOfCharacteristicPoints() - 1;
        int numberOfVariables = numberOfSegments * problem.getNumberOfCriteria();

        if (epsilon == null) {
            numberOfVariables += 1;
        }

        double[] lhs = new double[numberOfVariables];
        double rhs = 0.0;

        for (int i = 0; i < problem.getNumberOfCriteria(); i++) {
            for (Map.Entry<Integer, Double> entry : valuesToVariables[this.first][i].entrySet()) {
                lhs[numberOfSegments * i + entry.getKey() - 1] += entry.getValue();
            }

            for (Map.Entry<Integer, Double> entry : valuesToVariables[this.second][i].entrySet()) {
                lhs[numberOfSegments * i + entry.getKey() - 1] -= entry.getValue();
            }
        }

        String direction;

        if (this.type == Type.Indifference) {
            direction = "=";
        } else if (this.type == Type.WeakPreference) {
            direction = ">=";
        } else if (this.type == Type.StrictPreference) {
            direction = ">=";

            if (epsilon == null) {
                lhs[lhs.length - 1] = -1.0;
            } else {
                rhs = epsilon;
            }
        } else {
            throw new IllegalArgumentException("type");
        }

//        StringBuilder sb = new StringBuilder();
//        sb.append("U(");
//        sb.append(a);
//        sb.append(")");
//        sb.append(comparison.toString());
//        sb.append("U(");
//        sb.append(b);
//        sb.append(")");

        lst.add(new Constraint(lhs, direction, rhs, ""));//sb.toString());

        return lst;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Preference{");
        sb.append(first);
        sb.append(type.toString());
        sb.append(second);
        sb.append("}");
        return sb.toString();
    }
}
