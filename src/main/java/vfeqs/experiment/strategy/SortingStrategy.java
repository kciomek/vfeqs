package vfeqs.experiment.strategy;

import vfeqs.experiment.Pair;
import vfeqs.model.RORClassification;

import java.util.Map;

public abstract class SortingStrategy extends Strategy<RORClassification> {
    protected SortingStrategy(Map resultParameters) {
        super(resultParameters);
    }

    protected String getSuffix() {
        StringBuilder sb = new StringBuilder();

        if (this.getResultParameters() != null) {
            Integer limit = (Integer) this.getResultParameters().get("questionsLimit");
            String stopCriterionStr = (String) this.getResultParameters().get("stopCriterionStr");

            if (limit != null) {
                sb.append("-Q").append(limit);
            }

            if (stopCriterionStr != null) {
                sb.append("-S/").append(stopCriterionStr);
            }
        }

        return sb.length() > 0 ? ":" + sb.deleteCharAt(0).toString() : "";
    }
}
