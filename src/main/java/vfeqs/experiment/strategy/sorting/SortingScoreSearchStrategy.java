package vfeqs.experiment.strategy.sorting;

import polyrun.SamplerRunner;
import polyrun.sampler.HitAndRun;
import polyrun.thinning.LinearlyScalableThinningFunction;
import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.Pair;
import vfeqs.experiment.strategy.SortingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORClassification;
import vfeqs.model.RORResult;
import vfeqs.model.preferenceinformation.AssignmentExample;
import vfeqs.optimization.GLPKForPolyrun;

import java.util.*;

public class SortingScoreSearchStrategy extends SortingStrategy {
    public enum Merger {
        W("W"), // Worse
        E("E"), // Expected
        M("M"); // Mean

        private final String stringValue;

        Merger(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    public enum Scorer {
        AIW("AIW"),
        APOI("APOI"),
        CAI("CAI"),
        QUE("QUE"),
        REG("REG");

        private final String stringValue;

        Scorer(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    private final Merger merger;
    private final Scorer scorer;
    private final SamplerRunner samplerRunner;

    public SortingScoreSearchStrategy(Merger merger, Scorer scorer, Map parameters) {
        super(parameters);

        this.merger = merger;
        this.scorer = scorer;
        this.samplerRunner = new SamplerRunner(new HitAndRun(new LinearlyScalableThinningFunction(1.0)), new GLPKForPolyrun());
    }


    @Override
    protected double scoreQuestion(RORClassification classification, ExactAssignmentQuestion question) {
        double score;

        Pair<List<Double>, StrategyResult> scores = this.score(classification, question);

        if (this.merger == Merger.W) {
            score = Collections.max(scores.getFirst());
        } else if (this.merger == Merger.M) {
            score = 0.0;

            for (Double s : scores.getFirst()) {
                score += s;
            }

            score /= scores.getFirst().size();
        } else {
            double caiSum = 0.0;
            score = 0.0;

            for (int answerIndex = 0; answerIndex < question.getNumberOfAnswers(); answerIndex++) {
                double cai = classification.getCAI(question.getAlternative(), ((AssignmentExample) question.getAnswerByIndex(answerIndex)).getClassIndex());
                score += cai * scores.getFirst().get(answerIndex);
                caiSum += cai;
            }

            if (score != 0) {
                score /= caiSum;
            }
        }

        return score;
    }

    private Pair<List<Double>, StrategyResult> score(final RORClassification classification, ExactAssignmentQuestion question) {
        final List<Double> scores = new ArrayList<Double>();
        final List<RORResult> successors = new ArrayList<RORResult>();

        for (int answerIndex = 0; answerIndex < question.getNumberOfAnswers(); answerIndex++) {
            RORClassification successor = (RORClassification) classification.createSuccessor(question, answerIndex);
            double score;

            if (this.scorer == Scorer.AIW) {
                score = successor.getAverageAIW();
            } else if (this.scorer == Scorer.APOI) {
                score = successor.getAverageAPOIEntropy();
            } else if (this.scorer == Scorer.CAI) {
                score = successor.getAverageCAIEntropy();
            } else if (this.scorer == Scorer.QUE) {
                score = successor.getNumberOfQuestions();
            } else { // this.scorer == Scorer.REG
                score = successor.getMaxMinmaxRegret();
            }

            scores.add(score);
            successors.add(successor);
        }

        return new Pair<List<Double>, StrategyResult>(scores, new StrategyResult(question, successors));
    }

    @Override
    public String toString() {
        return merger.toString() + "-" + this.scorer.toString() + this.getSuffix();
    }
}
