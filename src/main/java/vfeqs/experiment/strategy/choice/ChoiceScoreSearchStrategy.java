package vfeqs.experiment.strategy.choice;

import polyrun.SampleConsumer;
import polyrun.SamplerRunner;
import polyrun.constraints.ConstraintsSystem;
import polyrun.sampler.HitAndRun;
import polyrun.thinning.LinearlyScalableThinningFunction;
import vfeqs.experiment.Pair;
import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.strategy.ChoiceStrategy;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.PairwiseRelation;
import vfeqs.model.RORChoice;
import vfeqs.model.RORResult;
import vfeqs.model.preferenceinformation.PreferenceComparison;
import vfeqs.optimization.GLPKForPolyrun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChoiceScoreSearchStrategy extends ChoiceStrategy {
    public enum Merger {
        Worse("W"),
        Expected("E"),
        Mean("M");

        private final String stringValue;

        Merger(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    public enum Scorer {
        FRAI("FRAI"),
        POA("POA");

        private final String stringValue;

        Scorer(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    private final Merger merger;
    private final Scorer scorer;
    private final SamplerRunner samplerRunner;

    public ChoiceScoreSearchStrategy(Merger merger, Scorer scorer, Map parameters) {
        super(parameters);

        this.merger = merger;
        this.scorer = scorer;
        this.samplerRunner = new SamplerRunner(new HitAndRun(new LinearlyScalableThinningFunction(1.0)), new GLPKForPolyrun());
    }

    @Override
    public StrategyResult chooseQuestion(RORChoice choice) {
        List<StrategyResult> bestQuestions = new ArrayList<StrategyResult>();
        double maxScore = Double.POSITIVE_INFINITY;

        for (PairwiseComparisonQuestion pair : choice.getQuestions()) {
            double score;

            Pair<Pair<Double, Double>, StrategyResult> pairScore = this.score(choice, pair);
            double scoreAB = pairScore.getFirst().getFirst();
            double scoreBA = pairScore.getFirst().getSecond();

            if (this.merger == Merger.Worse) {
                score = Math.max(scoreAB, scoreBA);
            } else if (this.merger == Merger.Mean) {
                score = (scoreAB + scoreBA) / 2.0;
            } else {
                double abPWI = choice.getPWI(pair.getFirst(), pair.getSecond());
                double baPWI = choice.getPWI(pair.getSecond(), pair.getFirst());

                if (abPWI + baPWI != 0.0) {
                    score = ((abPWI * scoreAB + baPWI * scoreBA) / (abPWI + baPWI));
                } else {
                    score = 0.0;
                }
            }

            if (bestQuestions.size() == 0 || score < maxScore) {
                maxScore = score;
                bestQuestions.clear();
                bestQuestions.add(pairScore.getSecond());
            } else if (bestQuestions.size() > 0 && score == maxScore) {
                bestQuestions.add(pairScore.getSecond());
            }
        }

        return bestQuestions.get(new Random().nextInt(bestQuestions.size()));
    }


    private Pair<Pair<Double, Double>, StrategyResult> score(final RORChoice state, PairwiseComparisonQuestion pair) {
        double[] score = new double[] { 0.0, 0.0 };

        StrategyResult strategyResult = new StrategyResult(pair, new ArrayList<RORResult>());

        if (this.scorer == Scorer.FRAI) {
            // stochastic with resampling, but without constructing next states
            for (int v = 0; v < 2; v++) {
                List<polyrun.constraints.Constraint> constraints = new ArrayList<polyrun.constraints.Constraint>(state.getModel().getConstraints());

                if (v == 0) {
                    constraints.addAll(new PreferenceComparison(pair.getFirst(), pair.getSecond()).getConstraints(state.getProblem(), state.getEpsilon()));
                } else {
                    constraints.addAll(new PreferenceComparison(pair.getSecond(), pair.getFirst()).getConstraints(state.getProblem(), state.getEpsilon()));
                }

                final double[] frai = new double[state.getProblem().getNumberOfAlternatives()];
                final double[] candidatesNumber = new double[1];

                try {
                    this.samplerRunner.sample(new ConstraintsSystem(constraints), state.getNumberOfSamples(), new SampleConsumer() {
                        @Override
                        public void consume(double[] doubles) {
                            int best = state.getSolutionByModelVariableValues(doubles).getBest();

                            if (frai[best] == 0.0) {
                                candidatesNumber[0]++;
                            }

                            frai[best] += 1.0;
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                for (int i = 0; i < state.getProblem().getNumberOfAlternatives(); i++) {
                    frai[i] /= state.getNumberOfSamples();
                }

                for (int i = 0; i < state.getProblem().getNumberOfAlternatives(); i++) {
                    if (frai[i] > 0.0) {
                        score[v] += -frai[i] * Math.log(frai[i]) / Math.log(2);
                    }
                }

                if (Double.isNaN(score[v])) {
                    throw new RuntimeException();
                }

                score[v] = score[v] / (double) state.getProblem().getNumberOfAlternatives();
            }
        } else { // this.scorer == Scorer.POA) {
            for (int v = 0; v < 2; v++) {
                List<polyrun.constraints.Constraint> constraints = new ArrayList<polyrun.constraints.Constraint>(state.getModelWithEpsilonAsVariable().getConstraints());

                if (v == 0) {
                    constraints.addAll(new PreferenceComparison(pair.getFirst(), pair.getSecond()).getConstraints(state.getProblem(), null));
                } else {
                    constraints.addAll(new PreferenceComparison(pair.getSecond(), pair.getFirst()).getConstraints(state.getProblem(), null));
                }

                score[v] = 0;

                for (Integer i : state.getRelation().getPotentiallyOptimalAlternatives()) {
                    if (PairwiseRelation.calculateIfAlternativeIsPotentiallyOptimal(constraints, i, state.getProblem(), state.getEpsilon())) {
                        score[v] += 1.0;
                    }
                }
            }
        }

        return new Pair<Pair<Double, Double>, StrategyResult>(new Pair<Double, Double>(score[0], score[1]), strategyResult);
    }

    @Override
    public String toString() {
        return merger.toString() + "-" + this.scorer.toString() + (((Boolean) this.getResultParameters().get("onlyPOAQuestions")) ? "-P" : "");
    }
}
