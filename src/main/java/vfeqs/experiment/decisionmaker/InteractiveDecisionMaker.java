package vfeqs.experiment.decisionmaker;

import vfeqs.experiment.strategy.StrategyResult;
import vfeqs.model.RORResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InteractiveDecisionMaker implements DecisionMaker {
    @Override
    public List<RORResult> decide(RORResult result, StrategyResult strategyResult) {
        List<RORResult> lst = new ArrayList<RORResult>();

        long startTime = System.currentTimeMillis();
        Integer choice = null;

        Scanner scanner = new Scanner(System.in);

        while (choice == null) {
            System.out.println("QUESTION:\t " + strategyResult.getQuestion().toStringForUser());

            try {
                choice = strategyResult.getQuestion().getAnswerIndexByUserResponse(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = null;
            } catch (IllegalArgumentException e) {
                choice = null;
            } finally {
                if (choice == null) {
                    System.err.println("QUESTION: Wrong command!");
                    choice = null;
                }
            }
        }

        System.err.println("QUESTION:\tanswered in " + (System.currentTimeMillis() - startTime) + " ms\nQUESTION ------");

        RORResult successor = strategyResult.getSuccessor(choice);

        if (successor == null) {
            successor = result.createSuccessor(strategyResult.getQuestion(), choice);
        }

        lst.add(successor);

        return lst;
    }

    @Override
    public String getSummary() {
        return "-";
    }

    @Override
    public String toString() {
        return "interactive";
    }
}
