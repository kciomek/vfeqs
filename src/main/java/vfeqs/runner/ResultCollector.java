package vfeqs.runner;

import vfeqs.experiment.Pair;
import vfeqs.experiment.Experiment;

import java.util.List;

public interface ResultCollector {
    void putResult(Experiment experiment, List<Pair<Integer, Pair<Double, Double>>> result, long elapsedTime);
    void putError(Experiment experiment, Exception e);
}
