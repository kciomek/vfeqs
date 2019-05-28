package vfeqs.model;

import polyrun.thinning.ThinningFunction;
import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.Question;
import vfeqs.model.preferenceinformation.PreferenceInformation;
import vfeqs.model.preferenceinformation.PreferenceComparison;
import vfeqs.model.solution.VFRankingSolution;

import java.util.*;

public class RORRanking extends RORResult<VFRankingSolution, PairwiseComparisonQuestion> {

    private List<VFRankingSolution> samples;
    private final Double[][] poi;
    private final Double[][] rai;
    private final Integer[] br;
    private final Integer[] wr;

    private final PairwiseRelation relation;

    public RORRanking(VFProblem problem, int numberOfSamples, double minEpsilon, ThinningFunction thinningFunction, boolean calculateProbability, Map parameters) {
        super(problem, numberOfSamples, minEpsilon, thinningFunction, calculateProbability, parameters);

        this.poi = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfAlternatives()];
        this.rai = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfAlternatives()];
        this.br = new Integer[this.getProblem().getNumberOfAlternatives()];
        this.wr = new Integer[this.getProblem().getNumberOfAlternatives()];

        if (calculateProbability) {
            this.samples = this.generateSamples();
        } else {
            this.samples = null;
        }

        this.relation = new PairwiseRelation(this, false);
    }

    private RORRanking(RORRanking ranking, Question question, PreferenceInformation answer) {
        super(ranking, question, answer);

        this.poi = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfAlternatives()];
        this.rai = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfAlternatives()];
        this.br = new Integer[this.getProblem().getNumberOfAlternatives()];
        this.wr = new Integer[this.getProblem().getNumberOfAlternatives()];

        if (this.getCalculateProbability()) {
            this.samples = generateSamples();
        } else {
            this.samples = null;
        }

        PreferenceComparison p = (PreferenceComparison) answer;

        this.relation = ranking.relation.createSuccessor(this, p.getFirst(), p.getSecond());

       }

    private Iterable<VFRankingSolution> getSamples() {
        if (this.samples == null) {
            this.samples = this.generateSamples();
        }

        return this.samples;
    }

    public double getPOI(int a, int b) {
        if (this.poi[a][b] == null) {
            double poi = 0.0;

            for (VFRankingSolution sample : this.getSamples()) {
                if (sample.isWeaklyPreferred(a, b)) {
                    poi += 1.0;
                }
            }

            this.poi[a][b] = poi / (double) this.getNumberOfSamples();
        }

        return this.poi[a][b];
    }

    public double getPWI(int a, int b) {
        return 1.0 - this.getPOI(b, a);
    }

    public int getBestRank(int a) {
        if (this.br[a] == null) {
            int result = Integer.MAX_VALUE;

            for (VFRankingSolution sample : this.getSamples()) {
                result = Math.min(result, sample.getRank(a));
            }

            this.br[a] = result;
        }

        return this.br[a];
    }

    public int getWorstRank(int a) {
        if (this.wr[a] == null) {
            int result = Integer.MIN_VALUE;

            for (VFRankingSolution sample : this.getSamples()) {
                result = Math.max(result, sample.getRank(a));
            }

            this.wr[a] = result;
        }

        return this.wr[a];
    }

    public double getRAI(int a, int k) {
        if (this.rai[a][k - 1] == null) {
            double rai = 0.0;

            for (VFRankingSolution sample : this.getSamples()) {
                if (sample.getRank(a) == k) {
                    rai += 1.0;
                }
            }

            this.rai[a][k - 1] = rai / (double) this.getNumberOfSamples();
        }

        return this.rai[a][k - 1];
    }

    public double getERA() {
        double result = 0.0;

        for (int i = 0; i < getProblem().getNumberOfAlternatives(); i++) {
            result += (this.getWorstRank(i) - this.getBestRank(i));
        }

        return result / (double) this.getProblem().getNumberOfAlternatives();
    }

    public double getPE() { // global pairwise entropy
        double result = 0.0;

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            for (int j = 0; j < this.getProblem().getNumberOfAlternatives(); j++) {
                if (i != j) {
                    double poi = this.getPOI(i, j);

                    if (poi > 0) {
                        result += -poi * Math.log(poi) / Math.log(2);
                    }
                }
            }
        }

        if (Double.isNaN(result)) {
            throw new RuntimeException();
        }

        return result / (double) (this.getProblem().getNumberOfAlternatives() * (this.getProblem().getNumberOfAlternatives() - 1));
    }

    public Double getRE() {
        double result = 0.0;

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            for (int k = 1; k <= this.getProblem().getNumberOfAlternatives(); k++) {
                double rai = this.getRAI(i, k);
                if (rai > 0) {
                    result += -rai * Math.log(rai) / Math.log(2);
                }
            }
        }

        if (Double.isNaN(result)) {
            throw new RuntimeException();
        }

        return result / (double) this.getProblem().getNumberOfAlternatives();
    }

    public int getNumberOfNecessaryRelations() {
        int result = 0;

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            for (int j = 0; j < this.getProblem().getNumberOfAlternatives(); j++) {
                if (i != j) {
                    if (this.isNecessaryPreferred(i, j)) {
                        result++;
                    }
                }
            }
        }

        return result;
    }

    public int getNumberOfDifferentRankings() {
        Set<String> set = new HashSet<String>();

        for (VFRankingSolution solution : this.getSamples()) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
                sb.append(i).append("-").append(solution.getRank(i)).append(",");
            }

            set.add(sb.toString());
        }

        return set.size();
    }

    public boolean isNecessaryPreferred(int a, int b) {
        return !this.relation.isPossiblyPreferred(b, a);
    }

    @Override
    public VFRankingSolution getSolutionByModelVariableValues(double[] values) {
        return new VFRankingSolution(values, this.getProblem(), 1e-10);
    }

    @Override
    protected List<PairwiseComparisonQuestion> generateQuestions() {
        List<PairwiseComparisonQuestion> lst = new ArrayList<PairwiseComparisonQuestion>();

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives() - 1; i++) {
            for (int j = i + 1; j < this.getProblem().getNumberOfAlternatives(); j++) {
                if (i != j) {
                    if (!this.isNecessaryPreferred(i, j) && !this.isNecessaryPreferred(j, i)) {
                        lst.add(new PairwiseComparisonQuestion(i, j));
                    }
                }
            }
        }

        return lst;
    }

    @Override
    protected void syncQuestions(List<PairwiseComparisonQuestion> questions) {
        for (Iterator<PairwiseComparisonQuestion> iterator = questions.iterator(); iterator.hasNext(); ) {
            PairwiseComparisonQuestion pair = iterator.next();

            if (this.isNecessaryPreferred(pair.getFirst(), pair.getSecond()) || this.isNecessaryPreferred(pair.getSecond(), pair.getFirst())) {
                iterator.remove();
            }
        }
    }

    @Override
    protected RORResult createSuccessor(Question question, PreferenceInformation answer) {
        return new RORRanking(this, question, answer);
    }

    @Override
    protected double getAnswerProbability(PreferenceInformation pi) {
        PreferenceComparison p = (PreferenceComparison) pi;

        return this.getPWI(p.getFirst(), p.getSecond()) / (this.getPWI(p.getFirst(), p.getSecond()) + this.getPWI(p.getSecond(), p.getFirst()));
    }

    @Override
    public int getAnswerIndexByResult(Question question, int[] data) {
        PairwiseComparisonQuestion pair = (PairwiseComparisonQuestion) question;

        if (data[pair.getFirst()] == data[pair.getSecond()]) {
            throw new RuntimeException("Same rank not supported.");
        }

        return data[pair.getFirst()] < data[pair.getSecond()] ? 0 : 1;
    }

    @Override
    public boolean isAlternativeStopCriterionSatisfied() {
        return false;
    }

    @Override
    public String toString() {
        return this.relation.getNecessaryRelationStringRepresentation();
    }

}
