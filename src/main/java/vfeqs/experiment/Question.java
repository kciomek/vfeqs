package vfeqs.experiment;

import vfeqs.model.preferenceinformation.PreferenceInformation;

public interface Question {
    int getNumberOfAnswers();
    PreferenceInformation getAnswerByIndex(int index);
    Integer getAnswerIndexByUserResponse(String response);
    String toStringForUser();
}
