package vfeqs.experiment.strategy.sorting;

import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.strategy.SortingStrategy;
import vfeqs.model.RORClassification;

import java.util.Map;

public class NDOMSortingStrategy extends SortingStrategy {

    public NDOMSortingStrategy(Map resultParameters) {
        super(resultParameters);
    }

    @Override
    public double scoreQuestion(RORClassification rorClassification, ExactAssignmentQuestion question) {
        return rorClassification.getProblem().getPerformanceMatrix().getNumberOfDominanceRelated(question.getAlternative());
    }

    @Override
    public String toString() {
        return "NDOM" + this.getSuffix();
    }
}
