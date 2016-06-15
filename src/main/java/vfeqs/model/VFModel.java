package vfeqs.model;

import vfeqs.optimization.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VFModel {
    private final List<polyrun.constraints.Constraint> constraints;

    public VFModel() {
        this.constraints = new ArrayList<polyrun.constraints.Constraint>();
    }

    public VFModel(List<polyrun.constraints.Constraint> constraints) {
        this.constraints = new ArrayList<polyrun.constraints.Constraint>(constraints);
    }

    public void add(Constraint constraint) {
        this.constraints.add(constraint);
    }

    public List<polyrun.constraints.Constraint> getConstraints() {
        return this.constraints;
    }

    public int getNumberOfVariables() {
        return constraints.get(0).getLhs().length;
    }

    public double getBoundingBoxPseudoHyperVolume() {
        final double accuracy = 1e-7;

        GLPVariableOptimizer s = new GLPKVariableOptimizer();
        double volume = 1.0;

        int numberOfVariables = this.getNumberOfVariables();

        for (int i = 0; i < numberOfVariables; i++) {
            OptimizationResult minimizationResult;
            OptimizationResult maximizationResult;

            try {
                minimizationResult = s.optimize(GLPVariableOptimizer.Direction.Minimize, i, this);
                maximizationResult = s.optimize(GLPVariableOptimizer.Direction.Maximize, i, this);
            } catch (UnboundedSystemException e) {
                throw new RuntimeException(e);
            } catch (InfeasibleSystemException e) {
                throw new RuntimeException(e);
            }

            double length = maximizationResult.getValue() - minimizationResult.getValue();

            if (length > accuracy) {
                volume *= length;
            }
        }

        return volume;
    }

    public void addAll(Collection<Constraint> constraints) {
        this.constraints.addAll(constraints);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (polyrun.constraints.Constraint c : this.constraints) {
            sb.append(c.toString()).append("\n");
        }

        return sb.toString();
    }
}
