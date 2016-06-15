package vfeqs.experiment.strategy.ranking;

import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.strategy.RankingStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.experiment.strategy.ranking.scorer.ExpectedChangeScoreMerger;
import vfeqs.model.RORRanking;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;

public class DeepDVFSearchStrategy extends RankingStrategy {
    public enum PairScorer {
        Worse("W"),
        ArithmeticAverage("M"),
        GeometricAverage("G"),
        Harmonic("H"),
        Expected("E");

        private final String stringValue;

        PairScorer(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    private final ExpectedChangeScoreMerger expectedChangeScoreMerger;

    private final int maxDepth;
    private final boolean alphaBeta;
    private final PairScorer pairScorer;

    public DeepDVFSearchStrategy(int maxDepth, boolean alphaBeta, String pairScorer) {
        super(null);
        this.maxDepth = maxDepth;
        this.alphaBeta = alphaBeta;

        this.expectedChangeScoreMerger = new ExpectedChangeScoreMerger();

        if (pairScorer.equals("W")) {
            this.pairScorer = PairScorer.Worse;

        } else if (pairScorer.equals("H")) {
            this.pairScorer = PairScorer.Harmonic;
        } else if (pairScorer.equals("M")) {
            this.pairScorer = PairScorer.ArithmeticAverage;
        } else if (pairScorer.equals("G")) {
            this.pairScorer = PairScorer.GeometricAverage;
        } else if (pairScorer.equals("E")) {
            this.pairScorer = PairScorer.Expected;
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
        if (depth == 0) {
            return new DeepSearchStrategyResult(1.0, null);
        }

        int numberOfChildren = rorRanking.getQuestions().size();

        if (numberOfChildren == 0) {
            return new DeepSearchStrategyResult(1.0, null);
        } else {
            DeepSearchStrategyResult theChosenOne = new DeepSearchStrategyResult(-1.0, null);

            for (PairwiseComparisonQuestion pair : rorRanking.getQuestions()) {
                DeepSearchStrategyResult res = dmMove(rorRanking, pair, depth - 1, alfa, beta);

                if (res.score > theChosenOne.score) {
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
        double bestScore = Double.MAX_VALUE;

        RORRanking[] childRankings = new RORRanking[2];

        double[] pwi = new double[2];

        pwi[0] = ranking.getPWI(pair.getFirst(), pair.getSecond());
        pwi[1] = ranking.getPWI(pair.getSecond(), pair.getFirst());

        if (pwi[0] + pwi[1] == 0) {
            pwi[0] = 0.0;
            pwi[1] = 0.0;
            //todo result is already known
        } else {
            pwi[0] = pwi[0] / (pwi[0] + pwi[1]);
            pwi[1] = pwi[1] / (pwi[0] + pwi[1]);
        }

        if (this.pairScorer == PairScorer.Worse) {
            for (int i = 0; i < 2; i++) {
                childRankings[i] = (RORRanking) ranking.createSuccessor(pair, i);

                DeepSearchStrategyResult res = this.algorithmMove(childRankings[i], depth, alfa, beta);

                res = new DeepSearchStrategyResult(res.score * pwi[i], res.strategyResult);

                if (res.score < bestScore) {
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

            if (this.pairScorer == PairScorer.Harmonic) {
                if (res[0].score == 0 || res[1].score == 0) {
                    bestScore = 0;
                } else {
                    bestScore = 1.0 / (1.0 / res[0].score + 1.0 / res[1].score);
                }
            } else if (this.pairScorer == PairScorer.ArithmeticAverage) {
                bestScore = 0.5 * res[0].score + 0.5 * res[1].score;
            } else if (this.pairScorer == PairScorer.GeometricAverage) {
                bestScore = Math.sqrt(res[0].score * res[1].score);
            } else if (this.pairScorer == PairScorer.Expected) {
                bestScore = this.expectedChangeScoreMerger.merge(ranking, null, pair.getFirst(), pair.getSecond(), res[0].score, res[1].score);
            }
        }


        List<RORResult> lst = new ArrayList<RORResult>();
        lst.add(childRankings[0]);
        lst.add(childRankings[1]);

        return new DeepSearchStrategyResult(bestScore, new StrategyResult(pair, lst));
    }


    @Override
    public String toString() {
        return (this.alphaBeta ? "X" : "") + "DVF-" + this.pairScorer.toString() + "-" + this.maxDepth;
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
