package vfeqs.experiment.strategy;

import vfeqs.model.RORClassification;

import java.util.Map;

public abstract class SortingStrategy extends Strategy<RORClassification> {
    protected SortingStrategy(Map resultParameters) {
        super(resultParameters);
    }

    protected String getSuffix() {
        if (this.getResultParameters() != null) {
            Integer limit = (Integer) this.getResultParameters().get("questionsLimit");

            if (limit != null) {
                return "-Q" + limit;
            }
        }

        return "";
    }
}
