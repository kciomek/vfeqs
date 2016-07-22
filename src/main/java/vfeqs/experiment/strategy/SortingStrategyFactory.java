package vfeqs.experiment.strategy;


import vfeqs.experiment.strategy.sorting.*;

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
            return new APOISortingStrategy();
        } else if (strategyName.equals("AIW")) {
            return new AIWSortingStrategy();
        }  else if (strategyName.equals("DOM")) {
            return new DOMSortingStrategy();
        } else {
            throw new IllegalArgumentException("strategyName");
        }
    }
}
