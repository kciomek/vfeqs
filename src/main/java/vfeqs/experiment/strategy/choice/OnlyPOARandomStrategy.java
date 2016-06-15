package vfeqs.experiment.strategy.choice;

import vfeqs.experiment.strategy.RandomStrategy;

import java.util.HashMap;

public class OnlyPOARandomStrategy extends RandomStrategy {
    public OnlyPOARandomStrategy() {
        super(new HashMap() {{
            put("onlyPOAQuestions", true);
        }});
    }

    @Override
    public String toString() {
        return "RAND-P";
    }
}
