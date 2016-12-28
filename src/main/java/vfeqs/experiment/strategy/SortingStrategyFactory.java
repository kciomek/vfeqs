package vfeqs.experiment.strategy;


import vfeqs.experiment.Pair;
import vfeqs.experiment.strategy.sorting.*;

import java.util.HashMap;
import java.util.Map;

public class SortingStrategyFactory extends StrategyFactory {
    private Pair<String, Map> extractParameters(String strategyName) {
        String[] fields = strategyName.split("-");
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (fields.length > 1) {
            parameters.put("questionsLimit", Integer.parseInt(fields[1].substring(1)));
        }

        return new Pair<String, Map>(fields[0], parameters);
    }
    public Strategy create(String strategyName) {
        final Pair<String, Map> nameAndParameters = this.extractParameters(strategyName);
        final String name = nameAndParameters.getFirst();
        final Map parameters = nameAndParameters.getSecond();

        if (name.equals("RAND")) {
            return new RandomStrategy(parameters);
        } else if (name.equals("DVF")) {
            return new DVFSortingStrategy(parameters);
        } else if (name.equals("RDVF")) {
            return new RDVFSortingStrategy(parameters);
        } else if (name.equals("CAI")) {
            return new CAISortingStrategy(parameters);
        } else if (name.equals("APOI")) {
            return new APOISortingStrategy(parameters);
        } else if (name.equals("AIW")) {
            return new AIWSortingStrategy(parameters);
        }  else if (name.equals("DOM")) {
            return new DOMSortingStrategy(parameters);
        } else {
            throw new IllegalArgumentException("strategyName");
        }
    }
}
