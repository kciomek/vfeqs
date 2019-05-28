package vfeqs.experiment.strategy;

import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.model.RORClassification;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SortingStrategy extends Strategy<RORClassification> {
    private final int requestedIndex;
    private final SortingStrategy tieResolver;

    protected SortingStrategy(Map resultParameters) {
        super(resultParameters);

        Integer requestedIndex = (Integer) resultParameters.get("requestedIndex");
        this.requestedIndex = requestedIndex == null ? 0 : requestedIndex - 1;
        this.tieResolver = (SortingStrategy) resultParameters.get("tieResolver");
    }

    @Override
    public final StrategyResult chooseQuestion(RORClassification rorClassification) {
        return this.selectQuestionWithMinimalScore(rorClassification, rorClassification.getQuestions());
    }

    public final StrategyResult selectQuestionWithMinimalScore(RORClassification rorClassification, List<ExactAssignmentQuestion> questions) {
        PrioritizedScoredStrategyResults strategyResults = new PrioritizedScoredStrategyResults(this.requestedIndex, this.tieResolver);

        for (ExactAssignmentQuestion question : questions) {
            double score = this.scoreQuestion(rorClassification, question);
            strategyResults.add(score, new StrategyResult(question, new ArrayList<RORResult>()));
        }

        return strategyResults.get(rorClassification);
    }

    protected abstract double scoreQuestion(RORClassification classification, ExactAssignmentQuestion question);
}
