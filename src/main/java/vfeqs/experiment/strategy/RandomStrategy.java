package vfeqs.experiment.strategy;

import vfeqs.experiment.Question;
import vfeqs.model.RORResult;

import java.util.*;

public class RandomStrategy extends Strategy<RORResult> {
    private final Random random;

    protected RandomStrategy() {
        super(null);

        this.random = new Random();
    }

    protected RandomStrategy(Map parameters) {
        super(parameters);

        this.random = new Random();
    }

    @Override
    public StrategyResult chooseQuestion(RORResult state) {
        return new StrategyResult(
                (Question) state.getQuestions().get(this.random.nextInt(state.getQuestions().size())),
                new ArrayList<RORResult>());
    }

    @Override
    public String toString() {
        return "RAND" + getSuffix();
    }
}
