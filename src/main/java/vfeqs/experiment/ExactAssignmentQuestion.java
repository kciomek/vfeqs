package vfeqs.experiment;

import vfeqs.model.preferenceinformation.AssignmentExample;
import vfeqs.model.preferenceinformation.PreferenceInformation;

public class ExactAssignmentQuestion implements Question {
    private final Integer alternative;
    private int from;
    private int to;

    public ExactAssignmentQuestion(Integer alternative, int from, int to) {
        this.alternative = alternative;
        this.from = from;
        this.to = to;
    }

    @Override
    public int getNumberOfAnswers() {
        return to - from + 1;
    }

    @Override
    public PreferenceInformation getAnswerByIndex(int index) {
        if (from + index > to) {
            throw new IllegalArgumentException("Index exceeds assignment interval");
        }

        return new AssignmentExample(alternative, AssignmentExample.Type.EXACT, from + index);
    }

    @Override
    public Integer getAnswerIndexByUserResponse(String response) {
        int answer = Integer.parseInt(response);

        if (answer < from || answer > to) {
            throw new IllegalArgumentException("response");
        } else {
            return answer - from;
        }
    }

    @Override
    public String toStringForUser() {
        return "What is the class of alternative a" + this.getAlternative() + " from interval C" + (from) + "-C" + (to) + "?";
    }

    public Integer getAlternative() {
        return this.alternative;
    }

    @Override
    public String toString() {
        return "ExactAssignmentQuestion{" + getAlternative() + ":C" + (from ) + "-C" + (to) + "}";
    }

    public void setInterval(int cmin, int cmax) {
        this.from = cmin;
        this.to = cmax;
    }
}
