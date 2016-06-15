package vfeqs.runner;

import vfeqs.experiment.Experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Runner {
    public long run(final BlockingQueue<Experiment> queue,
                    final ResultCollector resultCollector,
                    final Experiment.ResultObserver resultObserver,
                    final boolean processAllQuestions,
                    final boolean calculateProbabilities,
                    final int numberOfThreads) throws InterruptedException {
        if (numberOfThreads < 1) {
            throw new IllegalArgumentException("number of threads is less than 1");
        }

        long startTime = System.currentTimeMillis();

        if (numberOfThreads == 1) {
            new ExperimentRunner().run(queue, resultCollector, resultObserver, processAllQuestions, calculateProbabilities);
        } else {
            List<Thread> threads = new ArrayList<Thread>();

            for (int i = 0; i < numberOfThreads; i++) {
                threads.add(new Thread() {
                    public void run() {
                        new ExperimentRunner().run(queue, resultCollector, resultObserver, processAllQuestions, calculateProbabilities);
                    }
                });
            }

            for (int i = 0; i < numberOfThreads; i++) {
                threads.get(i).start();
            }

            for (int i = 0; i < numberOfThreads; i++) {
                threads.get(i).join();
            }
        }

        return System.currentTimeMillis() - startTime;
    }
}
