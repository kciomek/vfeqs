package vfeqs.experiment.strategy;

import java.util.Map;

public abstract class Strategy<RESULT_TYPE> {
    private final Map resultParameters;

    protected Strategy(Map resultParameters) {
        this.resultParameters = resultParameters;
    }

    public abstract StrategyResult chooseQuestion(RESULT_TYPE rorRanking);

    public Map getResultParameters() {
        return resultParameters;
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
}
