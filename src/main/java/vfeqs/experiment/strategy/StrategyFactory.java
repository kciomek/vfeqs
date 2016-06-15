package vfeqs.experiment.strategy;

import java.util.ArrayList;
import java.util.List;

public abstract class StrategyFactory {

    public abstract Strategy create(String strategyName);

    public List<Strategy> create(String[] names) {
        List<Strategy> strategies = new ArrayList<Strategy>();

        for (String name : names) {
            strategies.add(this.create(name));
        }

        return strategies;
    }
}

