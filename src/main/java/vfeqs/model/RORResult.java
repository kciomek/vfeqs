package vfeqs.model;


import polyrun.SampleConsumer;
import polyrun.SamplerRunner;
import polyrun.constraints.ConstraintsSystem;
import polyrun.sampler.HitAndRun;
import polyrun.thinning.ThinningFunction;
import vfeqs.experiment.Question;
import vfeqs.model.preferenceinformation.PreferenceInformation;
import vfeqs.optimization.GLPKForPolyrun;

import java.util.*;

public abstract class RORResult<SAMPLE_TYPE, QUESTION_TYPE extends Question> {
    private final VFProblem problem;
    private final double epsilon;
    private final List<PreferenceInformation> preferenceInformation;
    private final VFModel model;
    private final VFModel modelWithEpsilonAsVariable;

    private boolean questionUpdateNeeded;
    private List<QUESTION_TYPE> questions;

    private final int numberOfSamples;
    private SamplerRunner samplerRunner;
    private final ThinningFunction thinningFunction;

    private final boolean calculateProbability;
    private double eProbability;
    private double wProbability;
    public final Map parameters;

    public RORResult(VFProblem problem, int numberOfSamples, double epsilon, ThinningFunction thinningFunction, boolean calculateProbability, Map parameters) {
        this.problem = problem;
        this.numberOfSamples = numberOfSamples;
        this.epsilon = epsilon;
        this.thinningFunction = thinningFunction;
        this.calculateProbability = calculateProbability;
        this.eProbability = 1.0;
        this.wProbability = 1.0;

        this.preferenceInformation = new ArrayList<PreferenceInformation>();

        this.questionUpdateNeeded = true;
        this.questions = null;

        this.model = this.problem.getModel(this.epsilon);
        this.modelWithEpsilonAsVariable = this.problem.getModel(null);

        this.parameters = parameters;
    }

    protected RORResult(RORResult result, Question question, PreferenceInformation pi) {
        this.problem = result.problem;
        this.numberOfSamples = result.numberOfSamples;
        this.epsilon = result.epsilon;
        this.thinningFunction = result.thinningFunction;

        this.calculateProbability = result.calculateProbability;

        this.preferenceInformation = new ArrayList<PreferenceInformation>(result.preferenceInformation);

        this.questions = new ArrayList<QUESTION_TYPE>(result.questions);

        this.model = new VFModel(result.model.getConstraints());
        this.modelWithEpsilonAsVariable = new VFModel(result.modelWithEpsilonAsVariable.getConstraints());

        if (this.calculateProbability) {
            this.eProbability = result.eProbability * result.getAnswerProbability(pi);
            this.wProbability = result.wProbability / question.getNumberOfAnswers();
        }

        this.questionUpdateNeeded = true;
        this.preferenceInformation.add(pi);
        this.model.addAll(pi.getConstraints(this.problem, this.epsilon));
        this.modelWithEpsilonAsVariable.addAll(pi.getConstraints(this.problem, null));

        this.parameters = result.parameters;
    }

    private SamplerRunner getSamplerRunner() {
        if (this.samplerRunner == null) {
            this.samplerRunner = new SamplerRunner(new HitAndRun(this.thinningFunction), new GLPKForPolyrun());
        }

        return this.samplerRunner;
    }

    protected List<SAMPLE_TYPE> generateSamples() {
        final List<SAMPLE_TYPE> result = new ArrayList<SAMPLE_TYPE>(numberOfSamples);

        try {
            this.getSamplerRunner().sample(new ConstraintsSystem(this.model.getConstraints()), this.numberOfSamples, new SampleConsumer() {
                @Override
                public void consume(double[] doubles) {
                    result.add(getSolutionByModelVariableValues(doubles));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public RORResult createSuccessor(Question question, int chosenAnswer) {
        return this.createSuccessor(question, question.getAnswerByIndex(chosenAnswer));
    }


    protected abstract SAMPLE_TYPE getSolutionByModelVariableValues(double[] doubles);

    protected abstract RORResult createSuccessor(Question question, PreferenceInformation pi);

    protected abstract List<QUESTION_TYPE> generateQuestions();

    protected abstract void syncQuestions(List<QUESTION_TYPE> questions);

    protected abstract double getAnswerProbability(PreferenceInformation pi);

    public abstract int getAnswerIndexByResult(Question question, int[] data);



    public VFProblem getProblem() {
        return problem;
    }

    public List<PreferenceInformation> getPreferenceInformation() {
        return this.preferenceInformation;
    }

    public List<QUESTION_TYPE> getQuestions() {
        if (this.questions == null) {
            this.questions = generateQuestions();
        } else if (this.questionUpdateNeeded) {
            syncQuestions(this.questions);
            this.questionUpdateNeeded = false;
        }

        if (this.parameters != null) {
            final Integer questionsLimit = (Integer) this.parameters.get("questionsLimit");

            if (questionsLimit != null && questionsLimit < this.questions.size()) {
                // TODO: provide random from outside
                Random random = new Random();
                HashSet<QUESTION_TYPE> result = new HashSet<QUESTION_TYPE>(questionsLimit);
                List<QUESTION_TYPE> lresult = new ArrayList<QUESTION_TYPE>(questionsLimit);

                for (int i = this.questions.size() - questionsLimit; i < this.questions.size(); i++) {
                    int pos = random.nextInt(i + 1);
                    QUESTION_TYPE item = this.questions.get(pos);

                    if (result.contains(item)) {
                        result.add(this.questions.get(i));
                        lresult.add(this.questions.get(i));
                    } else {
                        result.add(item);
                        lresult.add(item);
                    }
                }

                if (lresult.size() != questionsLimit) {
                    throw new RuntimeException("lresult.size() != questionsLimit" + lresult.size() + " " + questionsLimit);
                }

                return lresult;
            }
        }

        return this.questions;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    public VFModel getModel() {
        return model;
    }

    public VFModel getModelWithEpsilonAsVariable() {
        return modelWithEpsilonAsVariable;
    }

    public double getEProbability() {
        return eProbability;
    }

    public double getWProbability() {
        return wProbability;
    }

    protected boolean getCalculateProbability() {
        return this.calculateProbability;
    }

    public int getNumberOfQuestions() {
        //fixme
        this.getQuestions();
        this.questionUpdateNeeded = true;
        return this.questions.size();
    }

    public abstract boolean isAlternativeStopCriterionSatisfied();
}
