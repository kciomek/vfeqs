package vfeqs.runner;

import polyrun.thinning.ConstantThinningFunction;
import polyrun.thinning.LinearlyScalableThinningFunction;
import polyrun.thinning.LogarithmicallyScalableThinningFunction;
import polyrun.thinning.ThinningFunction;
import vfeqs.experiment.Experiment;
import vfeqs.experiment.decisionmaker.*;
import vfeqs.experiment.strategy.*;
import vfeqs.model.*;

import java.io.FileNotFoundException;
import java.util.*;

public class ExperimentFactory {
    public enum Type {
        Ranking("R"),
        Choice("C"),
        Sorting("S");

        private final String stringValue;

        Type(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }

        public static Type fromString(String stringValue) {
            if (stringValue != null) {
                for (Type type : Type.values()) {
                    if (stringValue.equalsIgnoreCase(type.stringValue)) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    private static List<Integer> parseIntervalToList(String text) {
        final List<Integer> result = new ArrayList<Integer>();
        final String[] fields = text.split("-");

        Integer begin;
        Integer end;

        if (fields.length == 1) {
            begin = Integer.parseInt(fields[0]);
            end = begin;
        } else if (fields.length == 2) {
            begin = Integer.parseInt(fields[0]);
            end = Integer.parseInt(fields[1]);
        } else {
            throw new IllegalArgumentException("text");
        }

        for (int i = begin; i <= end; i++) {
            result.add(i);
        }

        return result;
    }

    private static DecisionMaker parseDecisionMaker(Type type, String text) {
        final String[] fields = text.split("/");
        DecisionMaker dm;

        if (fields[0].equals("full")) {
            dm = new FullTreeDecisionMaker();
        } else if (fields[0].equals("random")) {
            dm = new RandomDecisionMaker();
        } else if (fields[0].equals("interactive")) {
            dm = new InteractiveDecisionMaker();
        } else if (fields[0].equals("fixed")) {
            String[] t = fields[1].split("-");
            int[] data = new int[t.length];

            if (type == Type.Sorting) {
                for (int i = 0; i < t.length; i++) {
                    data[i] = Integer.parseInt(t[i]); // assignment
                }
            } else {
                for (int i = 0; i < t.length; i++) {
                    data[Integer.parseInt(t[i])] = i + 1; // rank
                }
            }

            dm = new FixedDecisionMaker(data);
        } else if (fields[0].equals("erandom")) {
            dm = null;
        } else {
            throw new IllegalArgumentException("Decision Maker " + fields[0] + " is not supported.");
        }

        return dm;
    }

    public Collection<Experiment> create(String line) throws FileNotFoundException {
        final String fields[] = line.split("\\s+");

        // type
        final Type type = Type.fromString(fields[0]);

        // priority
        String[] priorityString = fields[1].split("/");
        final int priority = Integer.parseInt(priorityString[0]);
        final boolean increasingPriority = priorityString.length == 2 && priorityString[1].equals("+");

        // problem
        String[] problemString = fields[2].split("@", 2);
        String[] problemSubString = problemString[0].split("/", 4);

        final List<Integer> alternatives = ExperimentFactory.parseIntervalToList(problemSubString[0]);
        final List<Integer> criteria = ExperimentFactory.parseIntervalToList(problemSubString[1]);
        final List<Integer> chPoints = ExperimentFactory.parseIntervalToList(problemSubString[2]);
        final List<Integer> classes = problemString.length == 1 ? Arrays.asList(0) : ExperimentFactory.parseIntervalToList(problemString[1]);

        final InstanceSeed instanceSeed = new InstanceSeed(problemSubString.length == 4 ? problemSubString[3] : null);

        // initial preference information

        String[] initialPreferenceInformation = fields[3].split("/");
        int numberOfInitialPreferenceInformationStatements = Integer.parseInt(initialPreferenceInformation[0]);
        Integer initialPreferenceInformationStatementsSeed;

        if (initialPreferenceInformation.length > 1) {
            initialPreferenceInformationStatementsSeed = Integer.parseInt(priorityString[1]);
        } else {
            initialPreferenceInformationStatementsSeed = new Random().nextInt();
        }

        // repetitions
        String[] repetitions = fields[4].split("/");

        final int numberOfInstances = Integer.parseInt(repetitions[0]);
        final int repetitionPerInstance = Integer.parseInt(repetitions[1]);
        final int internalRepetitions = Integer.parseInt(repetitions[2]);

        // decision maker
        final String dm = fields[5];

        // strategies
        final String[] strategies = fields[6].split(",");

        // epsilon
        final double minEpsilon = Double.parseDouble(fields[7]);

        // number of samples
        final int numberOfSamples = Integer.parseInt(fields[8]);

        // thinning function
        String[] thFunction = fields[9].split("/");
        final String thinningFunctionName = thFunction[0];
        final Double thinningFunctionParameter = thFunction.length == 1 ? null : Double.parseDouble(thFunction[1]);

        return this.create(type, priority, increasingPriority,
                alternatives, criteria, chPoints, classes,
                instanceSeed,
                numberOfInstances, repetitionPerInstance, internalRepetitions,
                dm, strategies,
                minEpsilon, numberOfSamples,
                thinningFunctionName, thinningFunctionParameter,
                numberOfInitialPreferenceInformationStatements,
                initialPreferenceInformationStatementsSeed
        );
    }

    public Collection<Experiment> create(
            Type type,
            int initialPriority,
            boolean increasePriority,
            Iterable<Integer> numberOfAlternatives,
            Iterable<Integer> numberOfCriteria,
            Iterable<Integer> numberOfChPoints,
            Iterable<Integer> numberOfClasses,
            InstanceSeed seed,
            int numberOfInstances,
            int numberOfRepetitions,
            int numberOfInternalRepetitions,
            String dmId,
            String[] strategiesIds,
            double minEpsilon,
            int numberOfSamples,
            String thinningFunctionName,
            Double thinningFunctionParameter,
            int numberOfInitialPreferenceInformationStatements,
            int initialPreferenceInformationStatementsSeed
    ) throws FileNotFoundException {
        List<Experiment> experiments = new ArrayList<Experiment>();

        RORResultFactory resultFactory;
        StrategyFactory strategyFactory;

        if (type == Type.Ranking) {
            resultFactory = new RORRankingFactory();
            strategyFactory = new RankingStrategyFactory();
        } else if (type == Type.Choice) {
            resultFactory = new RORChoiceFactory();
            strategyFactory = new ChoiceStrategyFactory();
        } else { // Type.Sorting
            resultFactory = new RORClassificationFactory();
            strategyFactory = new SortingStrategyFactory();
        }

        Iterable<Strategy> strategies = strategyFactory.create(strategiesIds);

        int priority = initialPriority;
        DecisionMaker decisionMaker = ExperimentFactory.parseDecisionMaker(type, dmId);

        for (int cls : numberOfClasses) {
            for (int p: numberOfChPoints) {
                for (int c : numberOfCriteria) {
                    for (int a : numberOfAlternatives) {
                        ThinningFunction thinningFunction;

                        if (thinningFunctionName.equals("tflg")) {
                            thinningFunction = new LogarithmicallyScalableThinningFunction(thinningFunctionParameter == null ? 1.0 : thinningFunctionParameter);
                        } else if (thinningFunctionName.equals("tfl")) {
                            thinningFunction = new LinearlyScalableThinningFunction(thinningFunctionParameter == null ? 1.0 : thinningFunctionParameter);
                        } else if (thinningFunctionName.equals("tfc")) {
                            thinningFunction = new ConstantThinningFunction(
                                    thinningFunctionParameter == null ? (int) Math.pow((c - 1) + (p - 1), 3) : thinningFunctionParameter.intValue()
                            );
                        } else {
                            throw new IllegalArgumentException("Thinning function " + thinningFunctionName + " is not supported.");
                        }

                        for (int i = 0; i < numberOfInstances; i++) {
                            VFProblem problem;

                            if (seed.getPath() == null) {
                                if ("g".equals(seed.getMethod())) {
                                    problem = new VFProblem(
                                            PerformanceMatrix.buildRandom(a, c,
                                                    PerformanceMatrix.GenerationMethod.ElementFromGauss,
                                                    seed.getSeed()),
                                            p, cls);
                                } else if ("u".equals(seed.getMethod())) {
                                    problem = new VFProblem(
                                            PerformanceMatrix.buildRandom(a, c,
                                                    PerformanceMatrix.GenerationMethod.ElementFromUniform,
                                                    seed.getSeed()),
                                            p, cls);
                                } else if ("h".equals(seed.getMethod())) {
                                    problem = new VFProblem(
                                            PerformanceMatrix.buildRandom(a, c,
                                                    PerformanceMatrix.GenerationMethod.VectorFromHypersphere,
                                                    seed.getSeed()),
                                            p, cls);
                                } else {
                                    throw new IllegalArgumentException("Unknown method " + seed.getMethod());
                                }
                            } else {
                                problem = new VFProblem(PerformanceMatrix.load(seed.getPath()), p, cls);
                            }

                            for (Strategy strategy : strategies) {
                                for (int j = 0; j < numberOfRepetitions; j++) {

                                    if (decisionMaker == null) {
                                        if (type == Type.Ranking || type == Type.Choice) {
                                            decisionMaker = new FixedDecisionMaker(RandomRanking.generate(problem, minEpsilon, thinningFunction));
                                        } else { // Type.Sorting
                                            decisionMaker = new FixedDecisionMaker(RandomAssignment.generate(problem, minEpsilon, thinningFunction));
                                        }
                                    }

                                    Experiment experiment = new Experiment(
                                            resultFactory,
                                            problem, strategy, decisionMaker, minEpsilon,
                                            numberOfSamples, thinningFunction,
                                            numberOfInternalRepetitions, priority
                                    );

                                    experiments.add(experiment);

                                    if (increasePriority) {
                                        priority++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return experiments;
    }

    private class InstanceSeed {
        private int seed;
        private String method;

        private String path;

        public InstanceSeed(String s) {
            seed = new Random().nextInt();
            method = "h";
            path = null;

            if (s != null) {
                try {
                    String[] f = s.split(":");
                    seed = Integer.parseInt(f[0]);

                    if (f.length > 1) {
                        method = f[1];
                    }
                } catch (NumberFormatException e) {
                    seed = 0;
                    method = null;
                    path = s;
                }
            }
        }

        public int getSeed() {
            return seed;
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }
    }
}
