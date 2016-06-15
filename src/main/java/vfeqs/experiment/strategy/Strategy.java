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
}
