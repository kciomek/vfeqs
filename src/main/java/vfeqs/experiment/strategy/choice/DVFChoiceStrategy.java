package vfeqs.experiment.strategy.choice;

import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.strategy.ChoiceStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORChoice;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DVFChoiceStrategy extends ChoiceStrategy {
    public DVFChoiceStrategy(Map resultParameters) {
        super(resultParameters);
    }

    @Override
    public StrategyResult chooseQuestion(RORChoice rorRanking) {
        // assuming that there is at least one pair to compare
        List<PairwiseComparisonQuestion> bestQuestions = new ArrayList<PairwiseComparisonQuestion>();
        double maxScore = Double.NEGATIVE_INFINITY;

        for (PairwiseComparisonQuestion question : rorRanking.getQuestions()) {
            double score = Math.min(rorRanking.getPWI(question.getFirst(), question.getSecond()), rorRanking.getPWI(question.getSecond(), question.getFirst()));

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
        return "DVF" + (((Boolean)this.getResultParameters().get("onlyPOAQuestions")) ? "-P" : "");
    }
}
