package vfeqs.experiment.decisionmaker;

import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORResult;

import java.util.List;

public interface DecisionMaker {
    List<RORResult> decide(RORResult state, StrategyResult strategyResult);
    String getSummary();
}
