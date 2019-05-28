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

public class AIWSortingStrategy extends SortingStrategy {

    public AIWSortingStrategy (Map resultParameters) {
        super(resultParameters);
    }

    @Override
    public double scoreQuestion(RORClassification classification, ExactAssignmentQuestion question) {
        return -question.getNumberOfAnswers();
    }

    @Override
    public String toString() {
        return "AIW" + this.getSuffix();
    }
}
