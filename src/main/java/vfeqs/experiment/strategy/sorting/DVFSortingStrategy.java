package vfeqs.experiment.strategy.sorting;

import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.strategy.SortingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORClassification;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DVFSortingStrategy extends SortingStrategy {

    public DVFSortingStrategy (Map resultParameters) {
        super(resultParameters);
    }

    @Override
    public StrategyResult chooseQuestion(RORClassification rorClassification) {
        // assuming that there is at least one pair to compare

        List<ExactAssignmentQuestion> bestQuestions = new ArrayList<ExactAssignmentQuestion>();
        double maxScore = Double.NEGATIVE_INFINITY;

        for (ExactAssignmentQuestion question : rorClassification.getQuestions()) {
            int numberOfAnswers = question.getNumberOfAnswers();

            double score = Double.POSITIVE_INFINITY;

            for (int i = 0; i < numberOfAnswers; i++) {
                double cai = rorClassification.getCAI(question.getAlternative(),
                        rorClassification.getContAssignmentRelation().getAnswer(question.getAlternative(), i));

                score = Math.min(score, cai);
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
        return "DVF" + this.getSuffix();
    }
}
