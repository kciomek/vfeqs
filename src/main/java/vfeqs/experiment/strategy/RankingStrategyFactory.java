package vfeqs.experiment.strategy;


import vfeqs.experiment.strategy.ranking.*;
import vfeqs.experiment.strategy.ranking.scorer.*;

public class RankingStrategyFactory extends StrategyFactory {

    public Strategy create(String strategyName) {
        if (strategyName.equals("RAND")) {
            return new RandomStrategy();
        } else if (strategyName.equals("SQEVAL")) {
            return new SQEvalStrategy();
        } else if (strategyName.equals("DVF")) {
            return new DVFStrategy();
        } else if (strategyName.equals("W-IN")) {
            return new ScoreBasedStrategy(new INScorer(), new WorstChangeScoreMerger());
        } else if (strategyName.equals("E-IN")) {
            return new ScoreBasedStrategy(new INScorer(), new ExpectedChangeScoreMerger());
        } else if (strategyName.equals("W-ERA")) {
            return new ScoreBasedStrategy(new ERAScorer(), new WorstChangeScoreMerger());
        } else if (strategyName.equals("E-ERA")) {
            return new ScoreBasedStrategy(new ERAScorer(), new ExpectedChangeScoreMerger());
        } else if (strategyName.equals("W-PE")) {
            return new ScoreBasedStrategy(new PEScorer(), new WorstChangeScoreMerger());
        } else if (strategyName.equals("E-PE")) {
            return new ScoreBasedStrategy(new PEScorer(), new ExpectedChangeScoreMerger());
        } else if (strategyName.equals("W-RE")) {
            return new ScoreBasedStrategy(new REScorer(), new WorstChangeScoreMerger());
        } else if (strategyName.equals("E-RE")) {
            return new ScoreBasedStrategy(new REScorer(), new ExpectedChangeScoreMerger());
        } else if (strategyName.equals("M-IN")) {
            return new ScoreBasedStrategy(new INScorer(), new MeanScoreMerger());
        } else if (strategyName.equals("M-ERA")) {
            return new ScoreBasedStrategy(new ERAScorer(), new MeanScoreMerger());
        } else if (strategyName.equals("M-PE")) {
            return new ScoreBasedStrategy(new PEScorer(), new MeanScoreMerger());
        } else if (strategyName.equals("M-RE")) {
            return new ScoreBasedStrategy(new REScorer(), new MeanScoreMerger());
        } else if (strategyName.startsWith("DS-")) {
            String[] f = strategyName.split("-");
            return new DeepSearchStrategy(Integer.parseInt(f[2]), false, f[1]);
        } else if (strategyName.startsWith("XDS-")) {
            String[] f = strategyName.split("-");
            return new DeepSearchStrategy(Integer.parseInt(f[2]), true, f[1]);
        }else if (strategyName.startsWith("DVF-")) {
            String[] f = strategyName.split("-");
            return new DeepDVFSearchStrategy(Integer.parseInt(f[2]), false, f[1]);
        }  else if (strategyName.startsWith("XDVF-")) {
            String[] f = strategyName.split("-");
            return new DeepDVFSearchStrategy(Integer.parseInt(f[2]), true, f[1]);
        } else {
            throw new IllegalArgumentException("strategyName");
        }
    }
}
