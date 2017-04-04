package vfeqs.model;

import polyrun.thinning.ThinningFunction;
import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.Pair;
import vfeqs.experiment.Question;
import vfeqs.model.preferenceinformation.AssignmentExample;
import vfeqs.model.preferenceinformation.PreferenceInformation;
import vfeqs.model.solution.VFClassificationSolution;

import java.util.*;

public class RORClassification extends RORResult<VFClassificationSolution, ExactAssignmentQuestion> {
    private final ContAssignmentRelation contAssignmentRelation;
    private final Double[][] cai;
    private final Double[][] apoi;

    private List<VFClassificationSolution> samples;

    public RORClassification(VFProblem problem, int numberOfSamples, double minEpsilon, ThinningFunction thinningFunction, boolean calculateProbability, Map parameters) {
        super(problem, numberOfSamples, minEpsilon, thinningFunction, calculateProbability, parameters);

        this.cai = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfClasses()];
        this.apoi = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfAlternatives()];

        if (calculateProbability) {
            this.samples = this.generateSamples();
        } else {
            this.samples = null;
        }

        this.contAssignmentRelation = new ContAssignmentRelation(this);
    }


    private RORClassification(RORClassification classification, Question question, PreferenceInformation answer) {
        super(classification, question, answer);

        this.cai = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfClasses()];
        this.apoi = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfAlternatives()];

        if (this.getCalculateProbability()) {
            this.samples = this.generateSamples();
        } else {
            this.samples = null;
        }

        AssignmentExample ae = (AssignmentExample) answer;

        this.contAssignmentRelation = classification.contAssignmentRelation.createSuccessor(this, classification, ae.getAlternative(), ae.getClassIndex());
    }

    public VFClassificationSolution getSolutionByModelVariableValues(double[] values) {
        return new VFClassificationSolution(values, this.getProblem(), 1e-10);
    }

    private Iterable<VFClassificationSolution> getSamples() {
        if (this.samples == null) {
            this.samples = this.generateSamples();
        }

        return this.samples;
    }

    public double getCAI(int alternative, int classIndex) {
        if (this.cai[alternative][classIndex] == null) {
            Double[] caiRow = new Double[this.getProblem().getNumberOfClasses()];

            for (int h = 0; h < caiRow.length; h++) {
                caiRow[h] = 0.0;
            }

            for (VFClassificationSolution sample : this.getSamples()) {
                caiRow[sample.getAssignment(alternative)] += 1.0;
            }

            for (int h = 0; h < caiRow.length; h++) {
                caiRow[h] = caiRow[h] / (double) this.getNumberOfSamples();
            }

            this.cai[alternative] = caiRow;
        }

        return this.cai[alternative][classIndex];
    }

    private double getAPOI(int alternative, int referenceAlternative) {
        if (this.apoi[alternative][referenceAlternative] == null) {
            int atLeastAsGood = 0;

            for (VFClassificationSolution sample : this.getSamples()) {
                if (sample.getAssignment(alternative) >= sample.getAssignment(referenceAlternative)) {
                    atLeastAsGood++;
                }
            }

            this.apoi[alternative][referenceAlternative] = atLeastAsGood / (double) this.getNumberOfSamples();
        }

        return this.apoi[alternative][referenceAlternative];
    }

    @Override
    protected RORResult createSuccessor(Question question, PreferenceInformation pi) {
        return new RORClassification(this, question, pi);
    }

    @Override
    protected List<ExactAssignmentQuestion> generateQuestions() {
        List<ExactAssignmentQuestion> list = new ArrayList<ExactAssignmentQuestion>();

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            if (this.contAssignmentRelation.getCmin(i) < this.contAssignmentRelation.getCmax(i)) {
                list.add(new ExactAssignmentQuestion(i, this.contAssignmentRelation.getCmin(i), this.contAssignmentRelation.getCmax(i)));
            }
        }

        return list;
    }

    @Override
    protected void syncQuestions(List<ExactAssignmentQuestion> questions) {
        List<ExactAssignmentQuestion> update = new ArrayList<ExactAssignmentQuestion>();

        for (Iterator<ExactAssignmentQuestion> iterator = questions.iterator(); iterator.hasNext(); ) {
            ExactAssignmentQuestion question = iterator.next();

            if (this.contAssignmentRelation.getCmin(question.getAlternative()) == this.contAssignmentRelation.getCmax(question.getAlternative())) {
                iterator.remove();
            } else {
                int cmin = this.contAssignmentRelation.getCmin(question.getAlternative());
                int cmax = this.contAssignmentRelation.getCmax(question.getAlternative());
                if (question.getFrom() != cmin || question.getTo() != cmax) {
                    iterator.remove();
                    update.add(new ExactAssignmentQuestion(question.getAlternative(), cmin, cmax));
                }
            }
        }

        questions.addAll(update);
    }

    @Override
    protected double getAnswerProbability(PreferenceInformation pi) {
        AssignmentExample ae = (AssignmentExample) pi;

        return this.getCAI(ae.getAlternative(), ae.getClassIndex());
    }

    @Override
    public int getAnswerIndexByResult(Question question, int[] data) {
        Integer alternative = ((ExactAssignmentQuestion) question).getAlternative();

        final int cmin = this.contAssignmentRelation.getCmin(alternative);
        final int cmax = this.contAssignmentRelation.getCmax(alternative);
        final int proposedAssignment = data[alternative];

        if (proposedAssignment < cmin || proposedAssignment > cmax) {
            throw new RuntimeException("Proposed assignment is out of possible assignment interval.");
        }

        return proposedAssignment - cmin;
    }

    @Override
    public boolean isAlternativeStopCriterionSatisfied() {
        Pair<String, Double> stopCriterion = (Pair<String, Double>) this.parameters.get("stopCriterion");

        if (stopCriterion != null) {
            if (stopCriterion.getFirst().equals("CAI")) {
                Double threshold = stopCriterion.getSecond();

                for (int alternative = 0; alternative < this.getProblem().getNumberOfAlternatives(); alternative++) {
                    boolean thereIsAtLeastOneOverThreshold = false;

                    for (int classIdx = 0; classIdx < this.getProblem().getNumberOfClasses(); classIdx++) {
                        if (this.getCAI(alternative, classIdx) >= threshold) {
                            thereIsAtLeastOneOverThreshold = true;
                            break;
                        }
                    }

                    if (!thereIsAtLeastOneOverThreshold) {
                        return false;
                    }
                }

                return true;
            } else {
                throw new RuntimeException("Unsupported stop criterion: " + stopCriterion.getFirst());
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            sb.append(i).append(":C");
            sb.append(this.contAssignmentRelation.getCmin(i));
            sb.append("-C");
            sb.append(this.contAssignmentRelation.getCmax(i));
            sb.append(",");
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public ContAssignmentRelation getContAssignmentRelation() {
        return contAssignmentRelation;
    }

    public double getAverageCAIEntropy() {
        double result = 0.0;

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            result += getCAIEntropy(i);
        }

        return result / (double) this.getProblem().getNumberOfAlternatives();
    }

    public double getAverageAPOIEntropy() {
        double result = 0.0;

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            result += getAPOIEntropy(i);
        }

        return result / (double) this.getProblem().getNumberOfAlternatives();
    }

    public double getAverageAIW() {
        double result = 0.0;

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            result += this.getAIW(i);
        }

        return result / (double) this.getProblem().getNumberOfAlternatives();
    }

    public double getCAIEntropy(int alternative) {
        double result = 0.0;

        for (int j = 0; j < this.getProblem().getNumberOfClasses(); j++) {
            double cai = this.getCAI(alternative, j);

            if (cai > 0) {
                result += -cai * Math.log(cai) / Math.log(2);
            }
        }

        return result;
    }

    public int getAIW(int alternative) {
        return this.contAssignmentRelation.getCmax(alternative) - this.contAssignmentRelation.getCmin(alternative) + 1;
    }

    public double getAPOIEntropy(int alternative) {
        double result = 0.0;

        for (int i = 0; i < this.getProblem().getNumberOfAlternatives(); i++) {
            if (i != alternative) { // apoi[i,i] = 1 => Math.log(apoi) = 0 => can be skipped
                double apoi = this.getAPOI(alternative, i);

                if (apoi > 0) {
                    result += -apoi * Math.log(apoi) / Math.log(2);
                }
            }
        }

        return result;
    }
}
