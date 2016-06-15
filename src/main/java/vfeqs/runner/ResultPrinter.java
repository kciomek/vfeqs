package vfeqs.runner;

import vfeqs.experiment.Pair;
import vfeqs.experiment.Statistics;
import vfeqs.experiment.Experiment;

import java.util.List;

public class ResultPrinter implements ResultCollector {

    @Override
    public synchronized void putResult(Experiment experiment, List<Pair<Integer, Pair<Double, Double>>> result, long elapsedTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(experiment.toCSVString())
                .append("\t")
                .append(elapsedTime)
                .append("\t")
                .append(result.size())
                .append("\t");

        sb.append("0/0");

        sb.append("\t").append(new Statistics(result).toString());

        System.out.println(sb.toString());
    }

    @Override
    public synchronized void putError(Experiment experiment, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR ")
                .append(experiment.toCSVString())
                .append("\t")
                .append(e.getMessage());

        System.err.println(sb.toString());

        e.printStackTrace();
    }
}
