package vfeqs.experiment.strategy.sorting;

import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.strategy.SortingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORClassification;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CAISortingStrategy extends SortingStrategy {

    public CAISortingStrategy() {
        super(null);
    }

    @Override
    public StrategyResult chooseQuestion(RORClassification rorClassification) {
        // assuming that there is at least one pair to compare

        List<ExactAssignmentQuestion> bestQuestions = new ArrayList<ExactAssignmentQuestion>();
        double maxScore = Double.NEGATIVE_INFINITY;

        for (ExactAssignmentQuestion question : rorClassification.getQuestions()) {
            int numberOfAnswers = question.getNumberOfAnswers();

            double score = 0.0;

            for (int i = 0; i < numberOfAnswers; i++) {
                double cai = rorClassification.getCAI(question.getAlternative(),
                        rorClassification.getContAssignmentRelation().getAnswer(question.getAlternative(), i));

                if (cai > 0) {
                    score += -cai * Math.log(cai) / Math.log(2);
                }
            }

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
        return "CAI";
    }
}
