package vfeqs.experiment.strategy.sorting;

import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.strategy.SortingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORClassification;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DOMSortingStrategy extends SortingStrategy {

    public DOMSortingStrategy (Map resultParameters) {
        super(resultParameters);
    }

    @Override
    public double scoreQuestion(RORClassification rorClassification, ExactAssignmentQuestion question) {
        return -rorClassification.getProblem().getPerformanceMatrix().getNumberOfDominanceRelated(question.getAlternative());
    }

    @Override
    public String toString() {
        return "DOM" + this.getSuffix();
    }
}
