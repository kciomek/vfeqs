package vfeqs.experiment.strategy;


import vfeqs.model.RORChoice;

import java.util.Map;

public abstract class ChoiceStrategy extends Strategy<RORChoice> {
    protected ChoiceStrategy(Map resultParameters) {
        super(resultParameters);
    }
}
