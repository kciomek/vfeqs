package vfeqs.model;

import vfeqs.model.preferenceinformation.AssignmentExample;
import vfeqs.model.solution.VFClassificationSolution;
import vfeqs.optimization.*;

import java.util.*;


public class ContAssignmentRelation {
    private final int[] cmin;
    private final int[] cmax;

    public ContAssignmentRelation(RORClassification owner) {
        this(owner, null, null, null);
    }

    private ContAssignmentRelation(RORClassification owner, RORClassification prev,  Integer alternative, Integer classIndex) {
        this.cmin = new int[owner.getProblem().getNumberOfAlternatives()];
        this.cmax = new int[owner.getProblem().getNumberOfAlternatives()];

        for (Integer i = 0; i < owner.getProblem().getNumberOfAlternatives(); i++) {
            if (i.equals(alternative)) {
                this.cmin[i] = classIndex;
                this.cmax[i] = classIndex;
            } else {
                int from;
                int to;

                if (prev == null) {
                    from = 0;
                    to = owner.getProblem().getNumberOfClasses() - 1;
                } else {
                    from = prev.getContAssignmentRelation().getCmin(i);
                    to = prev.getContAssignmentRelation().getCmax(i);
                }

                while (!lpPossible(owner, i, from)) {
                    from++;
                }

                while (to > from && !lpPossible(owner, i, to)) {
                    to--;
                }

                this.cmin[i] = from;
                this.cmax[i] = to;
            }
        }
    }

    public int getCmin(int alternative) {
        return this.cmin[alternative];
    }

    public int getCmax(int alternative) {
        return this.cmax[alternative];
    }

    private boolean lpPossible(RORClassification owner, int alternative, int classIndex) {
        boolean ret;

        VFModel model = new VFModel(owner.getModelWithEpsilonAsVariable().getConstraints());
        model.addAll(new AssignmentExample(alternative, AssignmentExample.Type.EXACT, classIndex).getConstraints(owner.getProblem(), null));
        GLPVariableOptimizer optimizer = new GLPKVariableOptimizer();

        try {
            OptimizationResult result = optimizer.optimize(GLPVariableOptimizer.Direction.Maximize, model.getNumberOfVariables() - 1, model);
            ret = result.getValue() >= owner.getEpsilon();
        } catch (InfeasibleSystemException e) {
            ret = false;
        } catch (UnboundedSystemException e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

    public ContAssignmentRelation createSuccessor(RORClassification owner, RORClassification prev, int alternative, int classIndex) {
        if (classIndex < this.cmin[alternative] || classIndex > this.cmax[alternative]) {
            throw new IllegalArgumentException("Inconsistent assignment: " + alternative + " -> " + classIndex + ".");
        }

        return new ContAssignmentRelation(owner, prev, alternative, classIndex);
    }


    public boolean isPossible(int alternative, int classIndex) {
        return this.cmin[alternative] <= classIndex && classIndex <= this.cmax[alternative];
    }

    public int getAssignmentIntervalWidth(int alternative) {
        return this.cmax[alternative] - this.cmin[alternative];
    }

    public Integer getAnswer(int alternative, int index) {
        return this.cmin[alternative] + index;
    }

    public Integer getIndexByAnswer(int alternative, int answer) {
        return answer - this.cmin[alternative];
    }
}
