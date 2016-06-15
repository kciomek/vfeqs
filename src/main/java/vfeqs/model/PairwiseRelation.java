package vfeqs.model;

import vfeqs.model.preferenceinformation.PreferenceComparison;
import vfeqs.optimization.*;

import java.util.*;

public class PairwiseRelation {
    private final Boolean[][] data; //is possible
    public final List<Integer> potentiallyOptimalAlternatives;
    private final Map<Integer, List<Integer>> necessaryRelationGraph;
    private final boolean calculatePotentiallyOptimalAlternativesSet;

    public PairwiseRelation(RORResult state, boolean calculatePotentiallyOptimalAlternativesSet) {
        this.data = new Boolean[state.getProblem().getNumberOfAlternatives()][state.getProblem().getNumberOfAlternatives()];
        this.potentiallyOptimalAlternatives = new ArrayList<Integer>();
        this.necessaryRelationGraph = new HashMap<Integer, List<Integer>>();
        this.calculatePotentiallyOptimalAlternativesSet = calculatePotentiallyOptimalAlternativesSet;

        for (int i = 0; i < state.getProblem().getNumberOfAlternatives(); i++) {
            this.data[i][i] = true;
            this.necessaryRelationGraph.put(i, new LinkedList<Integer>());
        }

        for (int i = 0; i < state.getProblem().getNumberOfAlternatives() - 1; i++) {
            for (int j = i + 1; j < state.getProblem().getNumberOfAlternatives(); j++) {
                if (this.calculatePossibleRelation(state, i, j)) {
                    this.data[i][j] = true;
                    this.data[j][i] = this.calculatePossibleRelation(state, j, i);

                    if (!this.data[j][i]) { // a_i ->N a_j
                        this.necessaryRelationGraph.get(i).add(j);
                    }
                } else {
                    // a_j ->N a_i
                    this.data[i][j] = false;
                    this.data[j][i] = true;

                    this.necessaryRelationGraph.get(j).add(i);
                }
            }
        }

        if (this.calculatePotentiallyOptimalAlternativesSet) {
            this.calculatePotentiallyOptimalAlternatives(state, null);
        }
    }

    private PairwiseRelation(int numberOfAlternatives, boolean calculatePotentiallyOptimalAlternativesSet) {
        this.potentiallyOptimalAlternatives = new ArrayList<Integer>();
        this.data = new Boolean[numberOfAlternatives][numberOfAlternatives];
        this.necessaryRelationGraph = new HashMap<Integer, List<Integer>>();
        this.calculatePotentiallyOptimalAlternativesSet = calculatePotentiallyOptimalAlternativesSet;
    }

    public PairwiseRelation createSuccessor(RORResult state, int preferred, int reference) {
        if (!this.isPossiblyPreferred(preferred, reference)) {
            throw new IllegalArgumentException("Preference information.");
        }

        PairwiseRelation successor = new PairwiseRelation(this.data.length, this.calculatePotentiallyOptimalAlternativesSet);

        for (int i = 0; i < state.getProblem().getNumberOfAlternatives(); i++) {
            successor.data[i][i] = true;
            successor.necessaryRelationGraph.put(i, new LinkedList<Integer>());
        }

        Stack<Integer> stack = new Stack<Integer>();
        stack.push(reference);

        while (!stack.isEmpty()) {
            Integer item = stack.pop();

            successor.data[preferred][item] = true;
            successor.data[item][preferred] = false;

            for (Integer i : this.necessaryRelationGraph.get(item)) {
                stack.push(i);
            }
        }

        for (int i = 0; i < state.getProblem().getNumberOfAlternatives() - 1; i++) {
            for (int j = i + 1; j < state.getProblem().getNumberOfAlternatives(); j++) {
                if (successor.data[i][j] == null) {
                    if (!this.data[i][j]) {
                        // a_j ->N a_i
                        successor.data[i][j] = false;
                        successor.data[j][i] = true;

                        successor.necessaryRelationGraph.get(j).add(i);
                    } else if (!this.data[j][i]) {
                        // a_i ->N a_j
                        successor.data[i][j] = true;
                        successor.data[j][i] = false;

                        successor.necessaryRelationGraph.get(i).add(j);
                    } else {
                        if (successor.calculatePossibleRelation(state, i, j)) {
                            successor.data[i][j] = true;
                            successor.data[j][i] = this.calculatePossibleRelation(state, j, i);

                            if (!successor.data[j][i]) {
                                // a_i ->N a_j
                                successor.necessaryRelationGraph.get(i).add(j);
                            }
                        } else {
                            // a_j ->N a_i
                            successor.data[i][j] = false;
                            successor.data[j][i] = true;

                            successor.necessaryRelationGraph.get(j).add(i);
                        }
                    }
                }
            }
        }

        if (this.calculatePotentiallyOptimalAlternativesSet) {
            successor.calculatePotentiallyOptimalAlternatives(state, this.getPotentiallyOptimalAlternatives());
        }

        return successor;
    }

    private boolean calculatePossibleRelation(RORResult state, int i, int j) {
        VFModel model = new VFModel(state.getModelWithEpsilonAsVariable().getConstraints());
        model.addAll(new PreferenceComparison(i, j).getConstraints(state.getProblem(), null));
        GLPVariableOptimizer optimizer = new GLPKVariableOptimizer();

        boolean possible;

        try {
            OptimizationResult result = optimizer.optimize(GLPVariableOptimizer.Direction.Maximize, model.getNumberOfVariables() - 1, model);
            possible = result.getValue() >= state.getEpsilon();
        } catch (InfeasibleSystemException e) {
            possible = false;
        } catch (UnboundedSystemException e) {
            throw new RuntimeException(e);
        }

        return possible;
    }

    private void calculatePotentiallyOptimalAlternatives(RORResult state, List<Integer> previousStatePOA) {
        for (int i = 0; i < state.getProblem().getNumberOfAlternatives(); i++) {
            if (previousStatePOA != null) {
                if (!previousStatePOA.contains(i)) {
                    continue;
                }
            }

            boolean isCandidateToBePotentiallyOptimal = true;

            for (int j = 0; j < state.getProblem().getNumberOfAlternatives(); j++) {
                if (i != j) {
                    if (!this.data[i][j]) {
                        isCandidateToBePotentiallyOptimal = false;
                        break;
                    }
                }
            }

            if (isCandidateToBePotentiallyOptimal) {
                if (PairwiseRelation.calculateIfAlternativeIsPotentiallyOptimal(
                        state.getModelWithEpsilonAsVariable().getConstraints(),
                        i,
                        state.getProblem(),
                        state.getEpsilon())) {
                    this.potentiallyOptimalAlternatives.add(i);
                }
            }
        }
    }

    public static boolean calculateIfAlternativeIsPotentiallyOptimal(List<polyrun.constraints.Constraint> constraints,
                                                                     Integer alternative,
                                                                     VFProblem problem,
                                                                     Double epsilon) {
        VFModel model = new VFModel(constraints);

        for (int j = 0; j < problem.getNumberOfAlternatives(); j++) {
            if (alternative != j) {
                model.addAll(new PreferenceComparison(alternative, j).getConstraints(problem, null));
            }
        }

        GLPVariableOptimizer optimizer = new GLPKVariableOptimizer();

        boolean isPoa;

        try {
            OptimizationResult result = optimizer.optimize(GLPVariableOptimizer.Direction.Maximize, model.getNumberOfVariables() - 1, model);
            isPoa = result.getValue() >= epsilon;
        } catch (InfeasibleSystemException e) {
            isPoa = false;
        } catch (UnboundedSystemException e) {
            throw new RuntimeException(e);
        }

        return isPoa;

    }

    public List<Integer> getPotentiallyOptimalAlternatives() {
        if (this.calculatePotentiallyOptimalAlternativesSet) {
            return this.potentiallyOptimalAlternatives;
        } else {
            throw new RuntimeException("Set of potentially optimal alternatives is not calculated. Use constructor parameter.");
        }
    }

    public boolean isPossiblyPreferred(int i, int j) {
        return this.data[i][j];
    }

    public String getNecessaryRelationStringRepresentation() {
        // todo move to separate class

        // https://stackoverflow.com/questions/1690953/transitive-reduction-algorithm-pseudocode
        final int N = data.length;

        boolean[][] m = new boolean[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                m[i][j] = !this.data[j][i];
            }
        }

        // reflexive reduction
        for (int i = 0; i < N; ++i)
            m[i][i] = false;

        // transitive reduction
        for (int j = 0; j < N; ++j)
            for (int i = 0; i < N; ++i)
                if (m[i][j])
                    for (int k = 0; k < N; ++k)
                        if (m[j][k])
                            m[i][k] = false;

        List<RankingEdge> edges = new ArrayList<RankingEdge>();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (m[i][j]) {
                    edges.add(new RankingEdge(i, j));
                }
            }
        }

        int i = 0;

        while (i < edges.size()) {
            RankingEdge edgeA = edges.get(i);

            for (int j = 0; j < edges.size(); j++) {
                RankingEdge edgeB = edges.get(j);

                if (edgeA.dest == edgeB.source) {
                    edgeB.pushFront(edgeA);
                    edges.remove(i);
                    i = -1;
                    break;
                } else if (edgeA.source == edgeB.dest) {
                    edgeB.pushBack(edgeA);
                    edges.remove(i);
                    i = -1;
                    break;
                }
            }

            i++;
        }

        StringBuilder sb = new StringBuilder();

        for (RankingEdge edge : edges) {
            sb.append(edge.toString()).append(",");
        }

        return sb.deleteCharAt(sb.length()-1).toString();
    }

    private class RankingEdge {
        private int source;
        private int dest;
        private final List<Integer> internalNodes;

        public RankingEdge(int source, int dest) {
            this.source = source;
            this.dest = dest;
            this.internalNodes = new LinkedList<Integer>();
        }

        public void pushBack(RankingEdge o) {
            this.internalNodes.add(this.dest);
            this.internalNodes.addAll(o.internalNodes);
            this.dest = o.dest;
        }

        public void pushFront(RankingEdge o) {
            this.internalNodes.add(0, this.source);
            this.internalNodes.addAll(0, o.internalNodes);
            this.source = o.source;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.source).append("-");

            for (Integer i : this.internalNodes) {
                sb.append(i).append("-");
            }

            sb.append(this.dest);

            return sb.toString();
        }
    }
}
