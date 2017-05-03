package vfeqs.experiment.strategy.sorting;

import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.strategy.SortingStrategy;
import vfeqs.model.RORClassification;

import java.util.Map;

public class REGSortingStrategy extends SortingStrategy {

    public REGSortingStrategy(Map resultParameters) {
        super(resultParameters);
    }

    @Override
    public double scoreQuestion(RORClassification rorClassification, ExactAssignmentQuestion question) {
        return -rorClassification.getMinmaxRegret(question.getAlternative());
    }

    @Override
    public String toString() {
        return "REG" + this.getSuffix();
    }
}
