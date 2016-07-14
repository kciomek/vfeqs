package vfeqs.experiment.strategy;


import vfeqs.experiment.strategy.sorting.AIWSortingStrategy;
import vfeqs.experiment.strategy.sorting.CAISortingStrategy;
import vfeqs.experiment.strategy.sorting.DVFSortingStrategy;
import vfeqs.experiment.strategy.sorting.RDVFSortingStrategy;

public class SortingStrategyFactory extends StrategyFactory {
    public Strategy create(String strategyName) {
        if (strategyName.equals("RAND")) {
            return new RandomStrategy();
        } else if (strategyName.equals("DVF")) {
            return new DVFSortingStrategy();
        } else if (strategyName.equals("RDVF")) {
            return new RDVFSortingStrategy();
        } else if (strategyName.equals("CAI")) {
            return new CAISortingStrategy();
        } else if (strategyName.equals("APOI")) {
            return new CAISortingStrategy();
        } else if (strategyName.equals("AIW")) {
            return new AIWSortingStrategy();
        } else {
            throw new IllegalArgumentException("strategyName");
        }
    }
}
