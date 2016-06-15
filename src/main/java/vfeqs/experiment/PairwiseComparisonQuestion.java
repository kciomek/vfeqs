package vfeqs.experiment;

import vfeqs.model.preferenceinformation.PreferenceComparison;
import vfeqs.model.preferenceinformation.PreferenceInformation;

public class PairwiseComparisonQuestion implements Question {
    private final int first;
    private final int second;

    public PairwiseComparisonQuestion(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    @Override
    public int getNumberOfAnswers() {
        return 2;
    }

    @Override
    public PreferenceInformation getAnswerByIndex(int index) {
        if (index == 0) {
            return new PreferenceComparison(this.getFirst(), this.getSecond());
        } else if (index == 1) {
            return new PreferenceComparison(this.getSecond(), this.getFirst());
        } else {
            throw new IllegalArgumentException("index");
        }
    }

    @Override
    public Integer getAnswerIndexByUserResponse(String response) {
        int answer = Integer.parseInt(response);

        if (answer == getFirst()) {
            return 0;
        } else if (answer == getSecond()) {
            return 1;
        } else {
            throw new IllegalArgumentException("response");
        }
    }

    @Override
    public String toStringForUser() {
        return "What is preferred: alternative " + this.getFirst() + " or " + this.getSecond() + "?";
    }

    @Override
    public String toString() {
        return "PairwiseComparisonQuestion{" + getFirst() + "," + getSecond() + "}";
    }
}
