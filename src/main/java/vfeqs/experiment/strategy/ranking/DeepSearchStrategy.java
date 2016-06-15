package vfeqs.experiment.strategy.ranking;

import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.strategy.RankingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.experiment.strategy.ranking.scorer.ExpectedChangeScoreMerger;
import vfeqs.model.RORRanking;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;

public class DeepSearchStrategy extends RankingStrategy {
    public enum Merger {
        Worse("W"),
        ArithmeticAverage("M"),
        GeometricAverage("G"),
        Harmonic("H"),
        Expected("E"),
        WorseExpected("F");

        private final String stringValue;

        Merger(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    private final ExpectedChangeScoreMerger expectedChangeScoreMerger;

    private final int maxDepth;
    private final boolean alphaBeta;
    private final Merger merger;

    public DeepSearchStrategy(int maxDepth, boolean alphaBeta, String pairScorer) {
        super(null);
        this.maxDepth = maxDepth;
        this.alphaBeta = alphaBeta;

        this.expectedChangeScoreMerger = new ExpectedChangeScoreMerger();

        if (pairScorer.equals("W")) {
            this.merger = Merger.Worse;
        } else if (pairScorer.equals("H")) {
            this.merger = Merger.Harmonic;
        } else if (pairScorer.equals("M")) {
            this.merger = Merger.ArithmeticAverage;
        } else if (pairScorer.equals("G")) {
            this.merger = Merger.GeometricAverage;
        } else if (pairScorer.equals("E")) {
            this.merger = Merger.Expected;
        } else if (pairScorer.equals("F")) {
            this.merger = Merger.WorseExpected;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public StrategyResult chooseQuestion(RORRanking rorRanking) {
        DeepSearchStrategyResult res = this.algorithmMove(rorRanking, this.maxDepth, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        return res.strategyResult;
    }

    private DeepSearchStrategyResult algorithmMove(RORRanking rorRanking, int depth, double alfa, double beta) {
        int numberOfChildren = rorRanking.getQuestions().size();

        if (numberOfChildren == 0) {
            return new DeepSearchStrategyResult((double)maxDepth - depth,
                    null);
        } else if (numberOfChildren == 1) {
            return new DeepSearchStrategyResult((double)numberOfChildren + (maxDepth - depth),
                    new StrategyResult(rorRanking.getQuestions().get(0), new ArrayList<RORResult>()));
        } else if (depth == 0) {
            return new DeepSearchStrategyResult((double)numberOfChildren + (maxDepth - depth),
                    null);
        } else {
            DeepSearchStrategyResult theChosenOne = new DeepSearchStrategyResult(Double.MAX_VALUE, null);

            for (PairwiseComparisonQuestion pair : rorRanking.getQuestions()) {
                DeepSearchStrategyResult res = dmMove(rorRanking, pair, depth - 1, alfa, beta);

                if (res.score < theChosenOne.score) {
                    theChosenOne = res;
                    alfa = res.score;

                    if (this.alphaBeta && beta >= alfa) {
                        break;
                    }
                }
            }

            return theChosenOne;
        }
    }

    private DeepSearchStrategyResult dmMove(RORRanking ranking, PairwiseComparisonQuestion pair, int depth, double alfa, double beta) {
        double bestScore = Double.NEGATIVE_INFINITY;

        RORRanking[] childRankings = new RORRanking[2];

        if (this.merger == Merger.Worse) {
            for (int i = 0; i < 2; i++) {
                childRankings[i] = (RORRanking) ranking.createSuccessor(pair, i);

                DeepSearchStrategyResult res = this.algorithmMove(childRankings[i], depth, alfa, beta);

                if (res.score > bestScore) {
                    bestScore = res.score;
                    beta = res.score;

                    if (this.alphaBeta && beta >= alfa) {
                        break;
                    }
                }
            }
        } else {
            DeepSearchStrategyResult[] res = new DeepSearchStrategyResult[2];


            for (int i = 0; i < 2; i++) {
                childRankings[i] = (RORRanking) ranking.createSuccessor(pair, i);
                res[i] = this.algorithmMove(childRankings[i], depth, alfa, beta);
            }

            if (this.merger == Merger.Harmonic) {
                if (res[0].score == 0 || res[1].score == 0) {
                    bestScore = 0;
                } else {
                    bestScore = 1.0 / (1.0 / res[0].score + 1.0 / res[1].score);
                }
            } else if (this.merger == Merger.ArithmeticAverage) {
                bestScore = 0.5 * res[0].score + 0.5 * res[1].score;
            } else if (this.merger == Merger.GeometricAverage) {
                bestScore = Math.sqrt(res[0].score * res[1].score);
            } else if (this.merger == Merger.Expected) {
                bestScore = this.expectedChangeScoreMerger.merge(ranking, null, pair.getFirst(), pair.getSecond(), res[0].score, res[1].score);
            } else if (this.merger == Merger.WorseExpected) {
                bestScore = Math.max(res[0].score * ranking.getPWI(pair.getFirst(), pair.getSecond()), res[1].score * ranking.getPWI(pair.getSecond(), pair.getFirst()));
            }
        }


        List<RORResult> lst = new ArrayList<RORResult>();
        lst.add(childRankings[0]);
        lst.add(childRankings[1]);

        return new DeepSearchStrategyResult(bestScore, new StrategyResult(pair, lst));
    }


    @Override
    public String toString() {
        return (this.alphaBeta ? "X" : "") + "DS-" + this.merger.toString() + "-" + this.maxDepth;
    }

    private class DeepSearchStrategyResult {
        private final StrategyResult strategyResult;
        private final Double score;

        private DeepSearchStrategyResult(Double score, StrategyResult strategyResult) {
            this.strategyResult = strategyResult;
            this.score = score;
        }
    }
}
