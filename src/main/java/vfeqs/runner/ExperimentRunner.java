package vfeqs.runner;


import vfeqs.experiment.Pair;
import vfeqs.experiment.Experiment;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ExperimentRunner {
    public void run(BlockingQueue<Experiment> queue,
                    ResultCollector resultCollector,
                    Experiment.ResultObserver resultObserver,
                    boolean processAllQuestions,
                    boolean calculateProbabilities) {
        Experiment experiment;

        while ((experiment = queue.poll()) != null) {
            try {
                long startTime = System.currentTimeMillis();
                List<Pair<Integer, Pair<Double, Double>>> result = new LinkedList<Pair<Integer, Pair<Double, Double>>>();
                for (int i = 0; i < experiment.getNumberOfInternalRepetitions(); i++) {
                    result.addAll(experiment.run(resultObserver, processAllQuestions, calculateProbabilities));
                }
                long elapsedTime = System.currentTimeMillis() - startTime;

                resultCollector.putResult(experiment, result, elapsedTime);
            } catch (Exception e) {
                resultCollector.putError(experiment, e);
            }
        }
    }
}
