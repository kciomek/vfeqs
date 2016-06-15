package vfeqs.experiment.decisionmaker;

import vfeqs.model.RORResult;
import vfeqs.experiment.strategy.StrategyResult;

import java.util.*;

public class RandomDecisionMaker implements DecisionMaker {

    @Override
    public List<RORResult> decide(RORResult result, StrategyResult strategyResult) {
        List<RORResult> lst = new ArrayList<RORResult>();

        int chosen = new Random().nextInt(strategyResult.getQuestion().getNumberOfAnswers());

        RORResult successor = strategyResult.getSuccessor(chosen);

        if (successor == null) {
            successor = result.createSuccessor(strategyResult.getQuestion(), chosen);
        }

        lst.add(successor);

        return lst;
    }

    @Override
    public String getSummary() {
        return "-"; // todo
    }

    @Override
    public String toString() {
        return "random";
    }
}
