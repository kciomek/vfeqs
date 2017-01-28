package vfeqs.experiment.strategy;

import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.Pair;
import vfeqs.model.RORClassification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class PrioritizedScoredStrategyResults {
    private final int requestedResultIndex;
    private final List<Pair<Double, List<StrategyResult>>> bestStrategyResults;
    private final SortingStrategy tieResolver;

    public PrioritizedScoredStrategyResults(int requestedResultIndex, SortingStrategy tieResolver) {
        if (requestedResultIndex < 0) {
            throw new IllegalArgumentException("requestedResultIndex");
        }

        this.tieResolver = tieResolver;
        this.requestedResultIndex = requestedResultIndex;
        this.bestStrategyResults = new ArrayList<Pair<Double, List<StrategyResult>>>();
    }

    public void add(Double score, StrategyResult strategyResult) {
        if (this.bestStrategyResults.size() == 0) {
            this.bestStrategyResults.add(new Pair<Double, List<StrategyResult>>(score, new ArrayList<StrategyResult>(Arrays.asList(strategyResult))));
        } else {
            for (int i = 0; i < this.bestStrategyResults.size(); i++) {
                if (score < this.bestStrategyResults.get(i).getFirst()) {
                    this.bestStrategyResults.add(i, new Pair<Double, List<StrategyResult>>(score, new ArrayList<StrategyResult>(Arrays.asList(strategyResult))));
                    break;
                } else if (score.equals(this.bestStrategyResults.get(i).getFirst())) {
                    this.bestStrategyResults.get(i).getSecond().add(strategyResult);
                    break;
                }
            }

            int counter = 0;

            for (int i = 0; i < this.bestStrategyResults.size() - 1; i++) {
                counter += bestStrategyResults.size();
            }

            if (counter > this.requestedResultIndex) {
                this.bestStrategyResults.remove(this.bestStrategyResults.size() - 1);
            }
        }
    }

    public StrategyResult get(RORClassification classification) {
        int counter = 0;

        List<StrategyResult> toSelect = null;

        for (Pair<Double, List<StrategyResult>> scoredStrategyResult : this.bestStrategyResults) {
            if (this.requestedResultIndex < scoredStrategyResult.getSecond().size() + counter) {
                toSelect = scoredStrategyResult.getSecond();
                        break;
            }

            counter += scoredStrategyResult.getSecond().size();
        }

        if (toSelect == null) {
            toSelect = this.bestStrategyResults.get(this.bestStrategyResults.size() - 1).getSecond();
        }

        if (this.tieResolver == null) {
            return toSelect.get(new Random().nextInt(toSelect.size()));
        } else if (toSelect.size() == 1){
            return toSelect.get(0);
        } else {
            List<ExactAssignmentQuestion> questions = new ArrayList<ExactAssignmentQuestion>();

            for (StrategyResult strategyResult : toSelect) {
                questions.add((ExactAssignmentQuestion) strategyResult.getQuestion());
            }

            return this.tieResolver.selectQuestionWithMinimalScore(classification, questions);
        }
    }
}