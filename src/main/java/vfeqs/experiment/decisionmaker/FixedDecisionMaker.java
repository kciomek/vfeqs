package vfeqs.experiment.decisionmaker;

import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;

public class FixedDecisionMaker implements DecisionMaker {
    private final String summary;
    private final int[] data; // ranks for ranking and choice, assignments for sorting

    public FixedDecisionMaker(int[] data) {
        this.data = data;

        StringBuilder sb = new StringBuilder();

        for (int i : data) {
            sb.append(i).append("-");
        }

        sb.deleteCharAt(sb.length()-1);

        this.summary = sb.toString();
    }

    @Override
    public List<RORResult> decide(RORResult result, StrategyResult strategyResult) {
        List<RORResult> lst = new ArrayList<RORResult>();

        int chosen = result.getAnswerIndexByResult(strategyResult.getQuestion(), this.data);

        RORResult successor = strategyResult.getSuccessor(chosen);

        if (successor == null) {
            successor = result.createSuccessor(strategyResult.getQuestion(), chosen);
        }

        lst.add(successor);

        return lst;
    }

    @Override
    public String getSummary() {
        return this.summary;
    }

    @Override
    public String toString() {
        return "fixed";
    }
}
