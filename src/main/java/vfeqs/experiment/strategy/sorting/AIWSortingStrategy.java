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
    public StrategyResult chooseQuestion(RORClassification rorClassification) {
        // assuming that there is at least one pair to compare

        List<ExactAssignmentQuestion> bestQuestions = new ArrayList<ExactAssignmentQuestion>();
        double maxScore = Double.NEGATIVE_INFINITY;

        for (ExactAssignmentQuestion question : rorClassification.getQuestions()) {
            double score = question.getNumberOfAnswers();

            if (bestQuestions.size() == 0 || score > maxScore) {
                maxScore = score;
                bestQuestions.clear();
                bestQuestions.add(question);
            } else if (bestQuestions.size() > 0 && score == maxScore) {
                bestQuestions.add(question);
            }
        }

        return new StrategyResult(bestQuestions.get(new Random().nextInt(bestQuestions.size())),
                new ArrayList<RORResult>());
    }

    @Override
    public String toString() {
        return "AIW" + this.getSuffix();
    }
}
