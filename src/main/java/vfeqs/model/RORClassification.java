package vfeqs.model;

import polyrun.thinning.ThinningFunction;
import vfeqs.experiment.ExactAssignmentQuestion;
import vfeqs.experiment.Question;
import vfeqs.model.preferenceinformation.AssignmentExample;
import vfeqs.model.preferenceinformation.PreferenceInformation;
import vfeqs.model.solution.VFClassificationSolution;
import vfeqs.model.solution.VFRankingSolution;

import java.util.*;

public class RORClassification extends RORResult<VFClassificationSolution, ExactAssignmentQuestion> {
    private final ContAssignmentRelation contAssignmentRelation;
    private final Double[][] cai;

    private List<VFClassificationSolution> samples;

    public RORClassification(VFProblem problem, int numberOfSamples, double minEpsilon, ThinningFunction thinningFunction, boolean calculateProbability, Map parameters) {
        super(problem, numberOfSamples, minEpsilon, thinningFunction, calculateProbability, parameters);

        this.cai = new Double[this.getProblem().getNumberOfAlternatives()][this.getProblem().getNumberOfClasses()];

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
        for (Iterator<ExactAssignmentQuestion> iterator = questions.iterator(); iterator.hasNext(); ) {
            ExactAssignmentQuestion question = iterator.next();

            if (this.contAssignmentRelation.getCmin(question.getAlternative()) == this.contAssignmentRelation.getCmax(question.getAlternative())) {
                iterator.remove();
            } else {
                question.setInterval(this.contAssignmentRelation.getCmin(question.getAlternative()),
                        this.contAssignmentRelation.getCmax(question.getAlternative()));
            }
        }
    }

    @Override
    protected double getAnswerProbability(PreferenceInformation pi) {
        AssignmentExample ae = (AssignmentExample) pi;

        return this.getCAI(ae.getAlternative(), ae.getClassIndex());
    }

    @Override
    public int getAnswerIndexByResult(Question question, int[] data) {
        Integer alternative = ((ExactAssignmentQuestion) question).getAlternative();

        return this.contAssignmentRelation.getIndexByAnswer(alternative, data[alternative]);
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
}
