package vfeqs.model.preferenceinformation;

import vfeqs.model.Constraint;
import vfeqs.model.VFProblem;

import java.util.Collection;

public interface PreferenceInformation {
    Collection<Constraint> getConstraints(VFProblem problem, Double epsilon);
}
