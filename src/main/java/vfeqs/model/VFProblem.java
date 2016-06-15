package vfeqs.model;

import java.util.*;

public class VFProblem {
    private final PerformanceMatrix performanceMatrix;
    private final int numberOfCharacteristicPoints;
    private final int numberOfClasses;
    private final int firstThresholdIndex;
    private final Map<Integer, Double>[][] valuesToVariables;

    public Map<Integer, Double>[][] getValuesToVariables() {
        return valuesToVariables;
    }

    public VFProblem(PerformanceMatrix performanceMatrix,
                     int numberOfCharacteristicPoints) {
        this(performanceMatrix, numberOfCharacteristicPoints, 0);
    }

    public VFProblem(PerformanceMatrix performanceMatrix,
                     int numberOfCharacteristicPoints, // todo: allow different number of chpoints for different criteria
                     int numberOfClasses) {  // only for sorting problem
        if (numberOfCharacteristicPoints < 2) {
            throw new IllegalArgumentException("numberOfCharacteristicPoints");
        }

        if (numberOfClasses < 0) {
            throw new IllegalArgumentException("numberOfClasses");
        }

        this.performanceMatrix = performanceMatrix;
        this.numberOfCharacteristicPoints = numberOfCharacteristicPoints;
        this.numberOfClasses = numberOfClasses;
        this.firstThresholdIndex = getNumberOfCriteria() * (numberOfCharacteristicPoints - 1);

        int numberOfAlternatives = this.getNumberOfAlternatives();
        int numberOfCriteria = this.getNumberOfCriteria();

        this.valuesToVariables = new Map[numberOfAlternatives][numberOfCriteria];

        for (int i = 0; i < numberOfAlternatives; i++) {
            for (int j = 0; j < numberOfCriteria; j++) {
                valuesToVariables[i][j] = new HashMap<Integer, Double>();
            }
        }

        SortedMap<Double, List<Integer>>[] criterionValues = new SortedMap[numberOfCriteria]; //mapping from criterion value to list of alternative indices with such value

        for (int i = 0; i < numberOfCriteria; i++) {
            criterionValues[i] = new TreeMap<Double, List<Integer>>();

            for (int j = 0; j < numberOfAlternatives; j++) {
                Double value = this.performanceMatrix.getData()[j][i];
                if (criterionValues[i].containsKey(value)) {
                    criterionValues[i].get(value).add(j);
                } else {
                    criterionValues[i].put(value, new ArrayList<Integer>(Arrays.asList(j)));
                }
            }

            if (criterionValues[i].size() < 2) {
                throw new IllegalArgumentException("Criterion " + i + " is superfluous!");
            }
        }

        for (int i = 0; i < numberOfCriteria; i++) {
            // defining values by characteristic points
            double intervalLength = (criterionValues[i].lastKey() - criterionValues[i].firstKey()) / (double) (numberOfCharacteristicPoints - 1);
            double coeff = 1.0 / intervalLength;
            for (int j = 0; j < numberOfAlternatives; j++) {
                double value = this.performanceMatrix.getData()[j][i];

                if (value == criterionValues[i].lastKey()) {
                    this.valuesToVariables[j][i].put(numberOfCharacteristicPoints - 1, 1.0);
                } else {
                    int lowerChPoint = (int) Math.floor((value - criterionValues[i].firstKey()) * coeff);

                    if (lowerChPoint >= numberOfCharacteristicPoints - 1) {
                        throw new IllegalArgumentException("lowerChPoint >= numberOfCharacteristicPoints - 1: This should never happen.");
                    }

                    double lowerValue = criterionValues[i].firstKey() + intervalLength * lowerChPoint;
                    double upperValue = criterionValues[i].firstKey() + intervalLength * (lowerChPoint + 1);

                    double lowerCoeff;
                    double upperCoeff;

                    if (value < lowerValue) {
                        lowerCoeff = 1.0;
                        upperCoeff = 0.0;
                    } else if (value >= upperValue) {
                        lowerCoeff = 0.0;
                        upperCoeff = 1.0;
                    } else {
                        lowerCoeff = (lowerValue - value) / (upperValue - lowerValue) + 1.0;
                        upperCoeff = (value - lowerValue) / (upperValue - lowerValue);
                    }

                    if (lowerChPoint > 0) {
                        this.valuesToVariables[j][i].put(lowerChPoint, lowerCoeff);
                    }

                    this.valuesToVariables[j][i].put(lowerChPoint + 1, upperCoeff);
                }
            }
        }
    }

    public PerformanceMatrix getPerformanceMatrix() {
        return performanceMatrix;
    }

    public Integer getNumberOfAlternatives() {
        return this.performanceMatrix.getData().length;
    }

    public Integer getNumberOfCriteria() {
        return this.performanceMatrix.getData()[0].length;
    }

    public int getNumberOfCharacteristicPoints() {
        return numberOfCharacteristicPoints;
    }

    public int getFirstThresholdIndex() { // only for sorting problem
        return firstThresholdIndex;
    }

    public int getNumberOfClasses() { // only for sorting problem
        return numberOfClasses;
    }

    public VFModel getModel(Double epsilon) {
        VFModel model = new VFModel();

        int numberOfSegments = numberOfCharacteristicPoints - 1;
        int numberOfVariables = numberOfSegments * getNumberOfCriteria();

        if (epsilon == null) {
            numberOfVariables += 1;
        }

        if (numberOfClasses > 1) {
            numberOfVariables += numberOfClasses - 1;
        }

        // sum to one
        double[] sumToOne = new double[numberOfVariables];
        for (int i = 0; i < getNumberOfCriteria(); i++) {
            sumToOne[numberOfSegments * i + numberOfCharacteristicPoints - 2] = 1.0;
        }


        model.add(new Constraint(sumToOne, "=", 1.0, "sum to one"));

        // monotonicity of characteristic points

        for (int i = 0; i < getNumberOfCriteria(); i++) {
            for (int j = 0; j < numberOfSegments; j++) {
                double[] lhs = new double[numberOfVariables];
                double rhs = 0.0;

                lhs[numberOfSegments * i + j] = -1.0;

                if (j > 0) {
                    lhs[numberOfSegments * i + j - 1] = 1.0;
                }

                if (epsilon == null) {
                    lhs[lhs.length - 1] = 1.0;
                } else {
                    rhs = -epsilon;
                }

                model.add(new Constraint(lhs, "<=", rhs, "monotonicity(" + i + " " + j + ")"));
            }
        }

        // monotonicity of thresholds

        for (int i = 0; i < numberOfClasses; i++) {
            double[] lhs = new double[numberOfVariables];
            double rhs = 0.0;

            if (i < numberOfClasses - 1) {
                lhs[firstThresholdIndex + i] = -1.0;
            }

            if (i > 0) {
                lhs[firstThresholdIndex + i - 1] = 1.0;
            }

            if (epsilon == null) {
                lhs[lhs.length - 1] = 1.0;
            } else {
                rhs = -epsilon;
            }

            if (i == numberOfClasses - 1) {
                rhs += 1;
            }

            model.add(new Constraint(lhs, "<=", rhs, "threshold monotonicity(" + i + ")"));
        }

        return model;
    }
}