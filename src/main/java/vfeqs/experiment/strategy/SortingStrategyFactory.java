package vfeqs.experiment.strategy;


import vfeqs.experiment.strategy.sorting.DVFSortingSearchStrategy;

public class SortingStrategyFactory extends StrategyFactory {
    public Strategy create(String strategyName) {
        if (strategyName.equals("RAND")) {
            return new RandomStrategy();
        } else if (strategyName.equals("DVF")) {
            return new DVFSortingSearchStrategy();
        } else {
            throw new IllegalArgumentException("strategyName");
        }
    }
}
