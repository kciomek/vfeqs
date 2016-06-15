package vfeqs.experiment.strategy;


import vfeqs.experiment.strategy.choice.ChoiceScoreSearchStrategy;
import vfeqs.experiment.strategy.choice.DVFChoiceStrategy;
import vfeqs.experiment.strategy.choice.OnlyPOARandomStrategy;

import java.util.HashMap;
import java.util.Map;

public class ChoiceStrategyFactory extends StrategyFactory {
    private static Map createChoiceParameters(Boolean onlyPOAQuestions) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("onlyPOAQuestions", onlyPOAQuestions);
        return result;
    }

    public Strategy create(String strategyName) {
        if (strategyName.equals("RAND")) {
            return new RandomStrategy();
        } else if (strategyName.equals("DVF")) {
            return new DVFChoiceStrategy(ChoiceStrategyFactory.createChoiceParameters(false));
        }  else if (strategyName.equals("RAND-P")) {
            return new OnlyPOARandomStrategy();
        } else if (strategyName.equals("DVF-P")) {
            return new DVFChoiceStrategy(ChoiceStrategyFactory.createChoiceParameters(true));
        }  else if (strategyName.equals("W-POA")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Worse,
                    ChoiceScoreSearchStrategy.Scorer.POA,
                    ChoiceStrategyFactory.createChoiceParameters(false));
        } else if (strategyName.equals("M-POA")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Mean,
                    ChoiceScoreSearchStrategy.Scorer.POA,
                    ChoiceStrategyFactory.createChoiceParameters(false));
        } else if (strategyName.equals("E-POA")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Expected,
                    ChoiceScoreSearchStrategy.Scorer.POA,
                    ChoiceStrategyFactory.createChoiceParameters(false));
        } else if (strategyName.equals("W-FRAI")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Worse,
                    ChoiceScoreSearchStrategy.Scorer.FRAI,
                    ChoiceStrategyFactory.createChoiceParameters(false));
        } else if (strategyName.equals("M-FRAI")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Mean,
                    ChoiceScoreSearchStrategy.Scorer.FRAI,
                    ChoiceStrategyFactory.createChoiceParameters(false));
        } else if (strategyName.equals("E-FRAI")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Expected,
                    ChoiceScoreSearchStrategy.Scorer.FRAI,
                    ChoiceStrategyFactory.createChoiceParameters(false));
        } else if (strategyName.equals("W-POA-P")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Worse,
                    ChoiceScoreSearchStrategy.Scorer.POA,
                    ChoiceStrategyFactory.createChoiceParameters(true));
        } else if (strategyName.equals("M-POA-P")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Mean,
                    ChoiceScoreSearchStrategy.Scorer.POA,
                    ChoiceStrategyFactory.createChoiceParameters(true));
        } else if (strategyName.equals("E-POA-P")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Expected,
                    ChoiceScoreSearchStrategy.Scorer.POA,
                    ChoiceStrategyFactory.createChoiceParameters(true));
        } else if (strategyName.equals("W-FRAI-P")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Worse,
                    ChoiceScoreSearchStrategy.Scorer.FRAI,
                    ChoiceStrategyFactory.createChoiceParameters(true));
        } else if (strategyName.equals("M-FRAI-P")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Mean,
                    ChoiceScoreSearchStrategy.Scorer.FRAI,
                    ChoiceStrategyFactory.createChoiceParameters(true));
        } else if (strategyName.equals("E-FRAI-P")) {
            return new ChoiceScoreSearchStrategy(
                    ChoiceScoreSearchStrategy.Merger.Expected,
                    ChoiceScoreSearchStrategy.Scorer.FRAI,
                    ChoiceStrategyFactory.createChoiceParameters(true));
        } else {
            throw new IllegalArgumentException("strategyName");
        }
    }
}
