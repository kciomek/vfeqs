package vfeqs.experiment.decisionmaker;

import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;

public class FullTreeDecisionMaker implements DecisionMaker {

    @Override
    public List<RORResult> decide(RORResult result, StrategyResult strategyResult) {
        List<RORResult> lst = new ArrayList<RORResult>();

        for (int i = 0; i < strategyResult.getQuestion().getNumberOfAnswers(); i++) {
            RORResult successor = strategyResult.getSuccessor(i);

            if (successor == null) {
                successor = result.createSuccessor(strategyResult.getQuestion(), i);
            }

            lst.add(successor);
        }

        return lst;
    }

    @Override
    public String getSummary() {
        return "-";
    }

    @Override
    public String toString() {
        return "full";
    }
}
