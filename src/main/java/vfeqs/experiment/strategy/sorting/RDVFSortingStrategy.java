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

public class RDVFSortingStrategy extends SortingStrategy {

    public RDVFSortingStrategy(Map resultParameters) {
        super(resultParameters);
    }

    @Override
    protected double scoreQuestion(RORClassification rorClassification, ExactAssignmentQuestion question) {
        int numberOfAnswers = question.getNumberOfAnswers();

        double score = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < numberOfAnswers; i++) {
            double cai = rorClassification.getCAI(question.getAlternative(),
                    rorClassification.getContAssignmentRelation().getAnswer(question.getAlternative(), i));

            score = Math.max(score, cai);
        }

        return score;
    }

    @Override
    public String toString() {
        return "RDVF" + this.getSuffix();
    }
}
