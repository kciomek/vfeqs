package vfeqs.runner;


import vfeqs.experiment.Experiment;
import vfeqs.model.RORChoice;
import vfeqs.model.RORClassification;
import vfeqs.model.RORRanking;
import vfeqs.model.RORResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Main {
    public static void main(String[] args) throws Exception {
        CLI cli = new CLI();
        cli.parse(args);

        final int numberOfThreads = cli.getNumberOfThreads();
        final boolean printFinalResult = cli.isPrintFinalResult();
        final boolean printIterationInfo = cli.isPrintStep();
        final boolean calculateProbabilities = cli.isCalculateProbabilities();
        final boolean processAllQuestions = printFinalResult || calculateProbabilities || cli.isProcessAllQuestions();
        final BlockingQueue<Experiment> queue = new PriorityBlockingQueue<Experiment>();

        final ExperimentFactory experimentFactory = new ExperimentFactory();
        final ResultCollector resultCollector = new ResultPrinter();

        Experiment.ResultObserver resultObserver = null;

        if (printIterationInfo || printFinalResult) {
            resultObserver = new Experiment.ResultObserver() {
                @Override
                public void notify(Experiment experiment, RORResult result) {
                    if (printIterationInfo) {
                        StringBuilder sb = new StringBuilder();

                        if (result instanceof RORRanking) {
                            RORRanking ranking = (RORRanking) result;

                            sb.append("PROGRESS\t")
                                    .append(experiment.toCSVString())
                                    .append("\t")
                                    .append(ranking.getPreferenceInformation().size())
                                    .append("\t")
                                    .append(experiment.getExperimentHash())
                                    .append("\t")
                                    .append(ranking.getNumberOfDifferentRankings())
                                    .append("\t")
                                    .append(ranking.getQuestions().size())
                                    .append("\t")
                                    .append(ranking.getNumberOfNecessaryRelations())
                                    .append("\t")
                                    .append(ranking.getPE())
                                    .append("\t")
                                    .append(ranking.getRE())
                                    .append("\t")
                                    .append(ranking.getERA());
                        } else if (result instanceof RORChoice) {
                            RORChoice choice = (RORChoice) result;

                            sb.append("PROGRESS\t")
                                    .append(experiment.toCSVString())
                                    .append("\t")
                                    .append(choice.getPreferenceInformation().size())
                                    .append("\t")
                                    .append(experiment.getExperimentHash())
                                    .append("\t")
                                    .append(choice.getQuestions().size())
                                    .append("\t")
                                    .append(choice.getRelation().getPotentiallyOptimalAlternatives().size())
                                    .append("\t")
                                    .append(choice.getFRAIEntropy());

                            sb.append("\t{");

                            for (Integer i : choice.getRelation().getPotentiallyOptimalAlternatives()) {
                                sb.append(i).append(",");
                            }

                            sb.deleteCharAt(sb.length() - 1).append("}");
                        } else {
                            RORClassification classification = (RORClassification) result;

                            sb.append("PROGRESS\t")
                                    .append(experiment.toCSVString())
                                    .append("\t")
                                    .append(classification.getPreferenceInformation().size())
                                    .append("\t")
                                    .append(experiment.getExperimentHash())
                                    .append("\t")
                                    .append(classification.getQuestions().size())
                                    .append("\t")
                                    .append(classification.getAverageCAIEntropy())
                                    .append("\t")
                                    .append(classification.getAverageAIW());
                        }

                        System.out.println(sb.toString());
                    }

                    if (printFinalResult && result.getQuestions().size() == 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("RESULT\t")
                                .append(experiment.toCSVString())
                                .append("\t")
                                .append(result.toString());

                        System.out.println(sb.toString());
                    }
                }
            };
        }

        String experimentDescription = cli.getExperimentDescription();

        if (experimentDescription == null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    for (Experiment experiment : experimentFactory.create(line)) {
                        queue.put(experiment);
                    }
                }
            }
        } else {
            for (Experiment experiment : experimentFactory.create(experimentDescription)) {
                queue.put(experiment);
            }
        }

        System.err.println("# Loaded " + queue.size() + " experiment(s).");
        System.err.println("# Processing in " + numberOfThreads + " thread(s)...");

        long elapsedTime = new Runner().run(queue, resultCollector, resultObserver, processAllQuestions, calculateProbabilities, numberOfThreads);

        System.err.println("# Done in " + elapsedTime + " ms = " + ((double) elapsedTime) / (60000) + " min = " + ((double) elapsedTime) / (3600000) + " h");
    }
}
