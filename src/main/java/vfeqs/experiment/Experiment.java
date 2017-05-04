package vfeqs.experiment;

import polyrun.thinning.ThinningFunction;
import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.experiment.decisionmaker.DecisionMaker;
import vfeqs.experiment.strategy.Strategy;
import vfeqs.model.RORResult;
import vfeqs.model.RORResultFactory;
import vfeqs.model.VFProblem;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Experiment implements Comparable<Experiment> {
    private final VFProblem problem;
    private final Strategy strategy;
    private final DecisionMaker dm;
    private final int numberOfSamples;
    private final double minEpsilon;
    private final int priority;
    private final int numberOfInternalRepetitions;
    private final ThinningFunction thinningFunction;
    private final String experimentHash;
    private final RORResultFactory resultFactory;

    public Experiment(RORResultFactory resultFactory,
                      VFProblem problem, Strategy strategy, DecisionMaker dm,
                      double minEpsilon, int numberOfSamples, ThinningFunction thinningFunction,
                      int numberOfInternalRepetitions, int priority) {
        this.strategy = strategy;
        this.dm = dm;
        this.numberOfSamples = numberOfSamples;
        this.minEpsilon = minEpsilon;
        this.problem = problem;
        this.priority = priority;
        this.numberOfInternalRepetitions = numberOfInternalRepetitions;
        this.thinningFunction = thinningFunction;
        this.resultFactory = resultFactory;
        this.experimentHash = Integer.toString(new Random().nextInt());
    }

    @Override
    public int compareTo(Experiment o) {
        return this.priority < o.priority ? -1 : 1;
    }

    public int getNumberOfInternalRepetitions() {
        return numberOfInternalRepetitions;
    }

    public String getExperimentHash() {
        return experimentHash;
    }

    public interface ResultObserver {
        void notify(Experiment experiment, RORResult ranking);
    }

    public List<Pair<Integer, Pair<Double, Double>>> run(ResultObserver observer, boolean processAll, boolean calculateProbability) {
        List<Pair<Integer, Pair<Double, Double>>> result = new LinkedList<Pair<Integer, Pair<Double, Double>>>();

        Stack<RORResult> stack = new Stack<RORResult>();

        stack.push(this.resultFactory.create(problem, numberOfSamples, minEpsilon, thinningFunction, calculateProbability, this.strategy.getResultParameters()));

        while (!stack.isEmpty()) {
            RORResult current = stack.pop();

            boolean processed = false;

            while (!processed) {
                processed = true;

                if (observer != null) {
                    observer.notify(this, current);
                }

                int completeness = current.getNumberOfQuestions();

                if (current.isAlternativeStopCriterionSatisfied()) {
                    completeness = 0;
                }

                if (completeness == 0 || (completeness == 1 && !processAll)) {
                    for (int i = 0; i <= completeness; i++) {
                        result.add(new Pair<Integer, Pair<Double, Double>>(current.getPreferenceInformation().size() + completeness,
                                new Pair<Double, Double>(current.getWProbability(), current.getEProbability())));
                    }
                } else {
                    StrategyResult strategyResult = strategy.chooseQuestion(current);

                    for (RORResult child : dm.decide(current, strategyResult)) {
                        stack.push(child);
                    }
                }
            }
        }

        return result;
    }

    public VFProblem getProblem() {
        return problem;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public String toCSVString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.resultFactory.toString())
                .append("\t")
                .append(problem.getNumberOfAlternatives())
                .append("\t")
                .append(problem.getNumberOfCriteria())
                .append("\t")
                .append(problem.getNumberOfCharacteristicPoints())
                .append("\t")
                .append(problem.getNumberOfClasses())
                .append("\t");

        if (problem.getThresholds() == null) {
            sb.append("-");
        } else {
            DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);

            for (double val : problem.getThresholds()) {
                sb.append(df.format(val)).append(",");
            }

            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("\t")
                .append(problem.getPerformanceMatrix().getIdentifier())
                .append("\t")
                .append(problem.getPerformanceMatrix().getGenerationMethod().toString())
                .append("\t")
                .append("strict")
                .append("\t")
                .append(dm.toString())
                .append("\t")
                .append(dm.getSummary())
                .append("\t")
                .append(strategy.toString())
                .append("\t")
                .append(numberOfInternalRepetitions)
                .append("\t")
                .append(minEpsilon)
                .append("\t")
                .append(numberOfSamples)
                .append("\t")
                .append(thinningFunction.toString());

        return sb.toString();
    }
}
