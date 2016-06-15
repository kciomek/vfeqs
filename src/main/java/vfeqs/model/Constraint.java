package vfeqs.model;

public class Constraint implements polyrun.constraints.Constraint {
    private final double[] lhs;
    private String direction;
    private Double rhs;
    private final String comment;

    public Constraint(double[] lhs, String direction, Double rhs, String comment) {
        this.lhs = lhs;
        this.direction = direction;
        this.rhs = rhs;
        this.comment = comment;
    }

    public double[] getLhs() {
        return lhs;
    }

    public String getDirection() {
        return direction;
    }

    public double getRhs() {
        return rhs;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return this.toString(6);
    }

    public String toString(int cellSize) {
        StringBuilder sb = new StringBuilder();

        for (double lh : lhs) {
            sb.append(String.format("%" + cellSize + ".3f", lh));
        }

        sb.append(String.format(" %3s %" + cellSize + ".3f", this.getDirection(), this.getRhs()));

        if (this.comment != null) {
            sb.append(" ");
            sb.append(this.comment);
        }

        return sb.toString();
    }
}
