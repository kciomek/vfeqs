package vfeqs.experiment.strategy;

import vfeqs.experiment.Question;
import vfeqs.model.RORResult;

import java.util.List;

public class StrategyResult {
    private final List<RORResult> successors;
    private final Question question;

    public StrategyResult(Question question, List<RORResult> successors) {
        this.question = question;
        this.successors = successors;
    }

    public Question getQuestion() {
        return question;
    }

    public RORResult getSuccessor(int i) {
        if (i >= this.successors.size()) {
            return null;
        } else {
            return this.successors.get(i);
        }
    }
}
