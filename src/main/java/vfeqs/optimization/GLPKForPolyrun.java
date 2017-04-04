package vfeqs.optimization;


import org.gnu.glpk.*;
import polyrun.constraints.ConstraintsSystem;
import polyrun.exceptions.UnboundedSystemException;
import polyrun.solver.GLPSolver;
import polyrun.solver.SolverResult;

public class GLPKForPolyrun implements GLPSolver {
    @Override
    public SolverResult solve(Direction direction, double[] objective, ConstraintsSystem constraintsSystem) throws UnboundedSystemException {
        glp_prob lp;
        glp_smcp parm;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;

        SolverResult result;

        int numberOfConstraints = constraintsSystem.getA().length + constraintsSystem.getC().length;
        int numberOfVariables = constraintsSystem.getNumberOfVariables();

        try {
            GLPK.glp_java_set_msg_lvl(GLPKConstants.GLP_JAVA_MSG_LVL_OFF);
            GLPK.glp_term_out(0);

            lp = GLPK.glp_create_prob();
            GLPK.glp_add_cols(lp, numberOfVariables);

            for (int i = 0; i < numberOfVariables; i++) {
                GLPK.glp_set_col_kind(lp, i + 1, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, i + 1, GLPKConstants.GLP_FR, 0, 0);
            }

            GLPK.glp_add_rows(lp, numberOfConstraints);

            int constraintIndex = 1;

            for (int i = 0; i < constraintsSystem.getA().length; i++) {
                GLPK.glp_set_row_bnds(lp, constraintIndex, GLPKConstants.GLP_UP, 0, constraintsSystem.getB()[i]);

                ind = GLPK.new_intArray(numberOfVariables + 1);
                val = GLPK.new_doubleArray(numberOfVariables + 1);

                for (int j = 0; j < numberOfVariables; j++) {
                    GLPK.intArray_setitem(ind, j + 1, j + 1);
                    GLPK.doubleArray_setitem(val, j + 1, constraintsSystem.getA()[i][j]);
                }

                GLPK.glp_set_mat_row(lp, constraintIndex, numberOfVariables, ind, val);

                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);

                constraintIndex++;
            }


            for (int i = 0; i < constraintsSystem.getC().length; i++) {
                GLPK.glp_set_row_bnds(lp, constraintIndex, GLPKConstants.GLP_FX, constraintsSystem.getD()[i], constraintsSystem.getD()[i]);

                ind = GLPK.new_intArray(numberOfVariables + 1);
                val = GLPK.new_doubleArray(numberOfVariables + 1);

                for (int j = 0; j < numberOfVariables; j++) {
                    GLPK.intArray_setitem(ind, j + 1, j + 1);
                    GLPK.doubleArray_setitem(val, j + 1, constraintsSystem.getC()[i][j]);
                }

                GLPK.glp_set_mat_row(lp, constraintIndex, numberOfVariables, ind, val);

                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);

                constraintIndex++;
            }

            GLPK.glp_set_obj_dir(lp, direction == Direction.Minimize ? GLPKConstants.GLP_MIN : GLPKConstants.GLP_MAX);
            for (int j = 0; j < numberOfVariables; j++) {
                GLPK.glp_set_obj_coef(lp, j + 1, objective[j]);
            }

            parm = new glp_smcp();
            GLPK.glp_init_smcp(parm);
            ret = GLPK.glp_simplex(lp, parm);

            if (ret == 0) {
                int status = GLPK.glp_get_status(lp);

                if (status == GLPK.GLP_OPT) {
                    double[] solution = new double[numberOfVariables];

                    for (int i = 0; i < numberOfVariables; i++) {
                        solution[i] = GLPK.glp_get_col_prim(lp, i + 1);
                    }

                    result = new SolverResult(true, GLPK.glp_get_obj_val(lp), solution);
                } else if (status == GLPK.GLP_NOFEAS) {
                    result = new SolverResult(false, 0.0, null);
                } else if (status == GLPK.GLP_UNBND) {
                    result = null;
                } else {
                    GLPK.glp_delete_prob(lp);
                    throw new RuntimeException("Solver error with status = " + status + ".");
                }
            } else {
                GLPK.glp_delete_prob(lp);
                throw new RuntimeException("Solver error.");
            }
        } catch (GlpkException e) {
            throw new RuntimeException(e);
        }

        GLPK.glp_delete_prob(lp);

        if (result == null) {
            throw new UnboundedSystemException(null);
        } else {
            return result;
        }
    }
}
