package vfeqs.model;


import polyrun.thinning.ThinningFunction;
import vfeqs.experiment.PairwiseComparisonQuestion;
import vfeqs.experiment.Question;
import vfeqs.model.preferenceinformation.PreferenceInformation;
import vfeqs.model.preferenceinformation.PreferenceComparison;
import vfeqs.model.solution.VFChoiceSolution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RORChoice extends RORResult<VFChoiceSolution, PairwiseComparisonQuestion> {
    private List<VFChoiceSolution> samples;
    private final PairwiseRelation relation;

    private double[] frai;
    private final Double[][] poi;
    private final boolean onlyPOAQuestions;

    public RORChoice(VFProblem problem, int numberOfSamples, double epsilon, ThinningFunction thinningFunction, boolean calculateProbability, Map parameters) {
        super(problem, numberOfSamples, epsilon, thinningFunction, calculateProbability, parameters);

        if (calculateProbability) {
            this.samples = generateSamples();
        } else {
            this.samples = null;
        }

        this.relation = new PairwiseRelation(this, true);
        this.frai = null;
        this.poi = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfAlternatives()];

        if (this.parameters != null) {
            this.onlyPOAQuestions = (Boolean) this.parameters.get("onlyPOAQuestions");
        } else {
            this.onlyPOAQuestions = false;
        }
    }

    private RORChoice(RORChoice result, Question question, PreferenceInformation answer) {
        super(result, question, answer);

        this.onlyPOAQuestions = result.onlyPOAQuestions;

        this.frai = null;
        this.poi = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfAlternatives()];

        if (this.getCalculateProbability()) {
            this.samples = generateSamples();
        } else {
            this.samples = null;
        }

        PreferenceComparison p = (PreferenceComparison) answer;

        this.relation = result.relation.createSuccessor(this, p.getFirst(), p.getSecond());
    }

    public List<VFChoiceSolution> getSamples() {
        if (this.samples == null) {
            this.samples = this.generateSamples();
        }

        return samples;
    }

    public double getFRAI(int alternative) {
        if (this.frai == null) {
            this.frai = new double[this.getProblem().getNumberOfAlternatives()];

            for (VFChoiceSolution sample : this.getSamples()) {
                this.frai[sample.getBest()] += 1.0;
            }

            for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
                this.frai[i] /= this.getNumberOfSamples();
            }
        }

        return this.frai[alternative];
    }

    public double getFRAIEntropy() {
        double result = 0.0;

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            double e = this.getFRAI(i);

            if (e > 0) {
                result += -e * Math.log(e) / Math.log(2);
            }
        }

        if (Double.isNaN(result)) {
            throw new RuntimeException();
        }

        return result / (double) this.getProblem().getNumberOfAlternatives();
    }

    public double getPOI(int a, int b) {
        if (this.poi[a][b] == null) {
            double poi = 0.0;

            for (VFChoiceSolution sample : this.getSamples()) {
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

    @Override
    public VFChoiceSolution getSolutionByModelVariableValues(double[] doubles) {
        return new VFChoiceSolution(doubles, this.getProblem());
    }

    @Override
    protected RORResult createSuccessor(Question question, PreferenceInformation pi) {
        return new RORChoice(this, question, pi);
    }

    @Override
    protected List<PairwiseComparisonQuestion> generateQuestions() {
        List<PairwiseComparisonQuestion> lst = new ArrayList<PairwiseComparisonQuestion>();

        if (this.relation.getPotentiallyOptimalAlternatives().size() == 2) {
            lst.add(new PairwiseComparisonQuestion(this.relation.getPotentiallyOptimalAlternatives().get(0), this.relation.getPotentiallyOptimalAlternatives().get(1)));
        } else if (this.relation.getPotentiallyOptimalAlternatives().size() > 2) {
            if (this.onlyPOAQuestions) {
                for (int i = 0; i < this.relation.getPotentiallyOptimalAlternatives().size() - 1; i++) {
                    for (int j = i + 1; j < this.relation.getPotentiallyOptimalAlternatives().size(); j++) {
                        int a = this.relation.getPotentiallyOptimalAlternatives().get(i);
                        int b = this.relation.getPotentiallyOptimalAlternatives().get(j);

                        if (this.relation.isPossiblyPreferred(a, b) && this.relation.isPossiblyPreferred(b, a)) {
                            lst.add(new PairwiseComparisonQuestion(a, b));
                        }
                    }
                }
            } else {
                for (int i = 0; i < this.getProblem().getNumberOfAlternatives() - 1; i++) {
                    for (int j = i + 1; j < this.getProblem().getNumberOfAlternatives(); j++) {
                        if (this.relation.isPossiblyPreferred(i, j) && this.relation.isPossiblyPreferred(j, i)) {
                            lst.add(new PairwiseComparisonQuestion(i, j));
                        }
                    }
                }
            }
        }

        return lst;
    }

    @Override
    protected void syncQuestions(List<PairwiseComparisonQuestion> questions) {
        if (this.relation.getPotentiallyOptimalAlternatives().size() < 2) {
            questions.clear();
        } else if (this.relation.getPotentiallyOptimalAlternatives().size() == 2) {
            questions.clear();
            questions.add(new PairwiseComparisonQuestion(this.relation.getPotentiallyOptimalAlternatives().get(0), this.relation.getPotentiallyOptimalAlternatives().get(1)));
        } else {
            if (this.onlyPOAQuestions) {
                questions.clear();
                for (int i = 0; i < this.relation.getPotentiallyOptimalAlternatives().size() - 1; i++) {
                    for (int j = i + 1; j < this.relation.getPotentiallyOptimalAlternatives().size(); j++) {
                        int a = this.relation.getPotentiallyOptimalAlternatives().get(i);
                        int b = this.relation.getPotentiallyOptimalAlternatives().get(j);

                        if (this.relation.isPossiblyPreferred(a, b) && this.relation.isPossiblyPreferred(b, a)) {
                            questions.add(new PairwiseComparisonQuestion(a, b));
                        }
                    }
                }
            } else {
                for (Iterator<PairwiseComparisonQuestion> iterator = questions.iterator(); iterator.hasNext(); ) {
                    PairwiseComparisonQuestion pair = iterator.next();

                    if (!this.relation.isPossiblyPreferred(pair.getFirst(), pair.getSecond()) || !this.relation.isPossiblyPreferred(pair.getSecond(), pair.getFirst())) {
                        iterator.remove();
                    }
                }
            }
        }
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
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");

        for (Integer i : this.relation.getPotentiallyOptimalAlternatives()) {
            sb.append(i).append(",");
        }

        return sb.deleteCharAt(sb.length() - 1).append("}").toString();
    }

    public PairwiseRelation getRelation() {
        return relation;
    }
}
