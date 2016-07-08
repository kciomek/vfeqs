package vfeqs.experiment.strategy;

import vfeqs.model.RORClassification;

import java.util.Map;

public abstract class SortingStrategy extends Strategy<RORClassification> {
    protected SortingStrategy(Map resultParameters) {
        super(resultParameters);
    }
}
