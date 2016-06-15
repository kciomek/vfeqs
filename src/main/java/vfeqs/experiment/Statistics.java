package vfeqs.experiment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Statistics {
    private final double median;
    private double eMean;
    private double wMean;
    private int min;
    private int max;
    private double mean;
    private double sumEProbabilities;
    private double sumWProbabilities;

    public Statistics(List<Pair<Integer, Pair<Double, Double>>> lst) {
        if (lst.isEmpty()) {
            throw new IllegalArgumentException("lst cannot be empty");
        }

        this.min = Integer.MAX_VALUE;
        this.max = Integer.MIN_VALUE;
        this.mean = 0.0;
        this.eMean = 0.0;
        this.wMean = 0.0;
        this.sumEProbabilities = 0.0;
        this.sumWProbabilities = 0.0;

        for (Pair<Integer, Pair<Double, Double>> pair : lst) {
            Integer value = pair.getFirst();
            Double wProb = pair.getSecond().getFirst();
            Double eProb = pair.getSecond().getSecond();

            if (value < this.min) {
                this.min = value;
            }
            if (value > this.max) {
                this.max = value;
            }

            this.mean += value;
            this.sumEProbabilities += eProb;
            this.sumWProbabilities += wProb;
            this.eMean += (value * eProb);
            this.wMean += (value * wProb);
        }

        this.mean /= (double) lst.size();

        Integer[] l = new Integer[lst.size()];
        for (int i = 0; i < lst.size(); i++) {
            l[i] = lst.get(i).getFirst();
        }
        Arrays.sort(l);

        if (l.length == 1) {
            this.median = l[0];
        } else {
            int middle = l.length / 2;
            this.median = l.length % 2 == 0 ? (l[middle - 1] + l[middle]) / 2.0 : l[middle];
        }
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double getMean() {
        return mean;
    }

    public double getMedian() {
        return median;
    }

    public double getEMean() {
        return this.eMean;
    }

    public double getWMean() {
        return this.wMean;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getMin());
        sb.append("\t");
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
        sb.append(df.format(this.getMean()));
        sb.append("\t");
        sb.append(df.format(this.getMedian()));
        sb.append("\t");
        sb.append(this.getMax());
        sb.append("\t");
        sb.append(this.getWMean());
        sb.append("\t");
        sb.append(this.getEMean());
        sb.append("\t");
        sb.append(this.sumWProbabilities); //control value
        sb.append("\t");
        sb.append(this.sumEProbabilities); //control value

        return sb.toString();
    }
}
