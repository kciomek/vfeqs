package vfeqs.optimization;

import org.gnu.glpk.*;
import vfeqs.model.VFModel;

import java.util.List;

public class GLPKVariableOptimizer implements GLPVariableOptimizer {
    @Override
    public OptimizationResult optimize(Direction direction, int variableIndex, VFModel model) throws UnboundedSystemException, InfeasibleSystemException {
        glp_prob lp;
        glp_smcp parm;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;

        OptimizationResult result;

        List<polyrun.constraints.Constraint> constr = model.getConstraints();

        int numberOfConstraints = constr.size();

        try {
            GLPK.glp_java_set_msg_lvl(GLPKConstants.GLP_JAVA_MSG_LVL_OFF);
            GLPK.glp_term_out(0);

            lp = GLPK.glp_create_prob();
            GLPK.glp_add_cols(lp, model.getNumberOfVariables());

            for (int i = 0; i < model.getNumberOfVariables(); i++) {
                GLPK.glp_set_col_kind(lp, i + 1, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, i + 1, GLPKConstants.GLP_FR, 0, 0);
            }

            GLPK.glp_add_rows(lp, numberOfConstraints);
//
            for (int i = 0; i < numberOfConstraints; i++) {
                polyrun.constraints.Constraint constraint = constr.get(i);

                if (constraint.getDirection().equals("=")) {
                    GLPK.glp_set_row_bnds(lp, i + 1, GLPKConstants.GLP_FX, constraint.getRhs(), constraint.getRhs());
                } else if (constraint.getDirection().equals("<=")) {
                    GLPK.glp_set_row_bnds(lp, i + 1, GLPKConstants.GLP_UP, 0, constraint.getRhs());
                } else if (constraint.getDirection().equals(">=")) {
                    GLPK.glp_set_row_bnds(lp, i + 1, GLPKConstants.GLP_LO, constraint.getRhs(), 0);
                } else {
                    throw new IllegalArgumentException();
                }

                double[] lhs = constraint.getLhs();

                ind = GLPK.new_intArray(model.getNumberOfVariables() + 1);
                val = GLPK.new_doubleArray(model.getNumberOfVariables() + 1);

                for (int j = 0; j < model.getNumberOfVariables(); j++) {
                    GLPK.intArray_setitem(ind, j + 1, j + 1);
                    GLPK.doubleArray_setitem(val, j + 1, lhs[j]);
                }

                GLPK.glp_set_mat_row(lp, i + 1, model.getNumberOfVariables(), ind, val);

                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);
            }


            GLPK.glp_set_obj_dir(lp, direction == Direction.Minimize ? GLPKConstants.GLP_MIN : GLPKConstants.GLP_MAX);
            GLPK.glp_set_obj_coef(lp, variableIndex + 1, 1.0);

            parm = new glp_smcp();
            GLPK.glp_init_smcp(parm);
            ret = GLPK.glp_simplex(lp, parm);

            // Retrieve solution
            if (ret == 0) {
                int status = GLPK.glp_get_status(lp);

                if (status == GLPK.GLP_OPT) {
                    double[] solution = new double[model.getNumberOfVariables()];

                    for (int i = 0; i < model.getNumberOfVariables(); i++) {
                        solution[i] = GLPK.glp_get_col_prim(lp, i + 1);
                    }
                    result =  new OptimizationResult(GLPK.glp_get_obj_val(lp), solution);
                } else if (status == GLPK.GLP_NOFEAS) {
                    GLPK.glp_delete_prob(lp);
                    throw new InfeasibleSystemException(null);
                } else if (status == GLPK.GLP_UNBND) {
                    GLPK.glp_delete_prob(lp);
                    throw new UnboundedSystemException(null);
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

        return result;
    }
}

