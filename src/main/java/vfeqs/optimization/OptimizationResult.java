package vfeqs.optimization;

public class OptimizationResult {
    private final double[] solution;
    private final double value;

    public OptimizationResult(double value, double[] solution) {
        this.solution = solution;
        this.value = value;
    }

    public double[] getSolution() {
        return solution;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%5.3f] ", value));

        for (double aSolution : solution) {
            sb.append(String.format("%5.3f", aSolution));
            sb.append(" ");
        }

        return sb.toString();
    }
}
