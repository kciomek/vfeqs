package vfeqs.experiment.strategy;

import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.Pair;
import vfeqs.model.RORClassification;
import vfeqs.model.RORResult;

import java.util.*;

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

    protected String getSuffix() {
        StringBuilder sb = new StringBuilder();

        if (this.getResultParameters() != null) {
            Integer limit = (Integer) this.getResultParameters().get("questionsLimit");
            String stopCriterionStr = (String) this.getResultParameters().get("stopCriterionStr");
            Integer index = (Integer) this.getResultParameters().get("requestedIndex");
            String tieResolverStr = (String) this.getResultParameters().get("tieResolverStr");

            if (limit != null) {
                sb.append("-Q").append(limit);
            }

            if (stopCriterionStr != null) {
                sb.append("-S/").append(stopCriterionStr);
            }

            if (index != null) {
                sb.append("-P").append(index);
            }

            if (tieResolverStr != null) {
                sb.append("-T/").append(tieResolverStr);
            }
        }

        return sb.length() > 0 ? ":" + sb.deleteCharAt(0).toString() : "";
    }

    protected abstract double scoreQuestion(RORClassification classification, ExactAssignmentQuestion question);
}
