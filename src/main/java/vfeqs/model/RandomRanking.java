package vfeqs.model;

import polyrun.SamplerRunner;
import polyrun.constraints.ConstraintsSystem;
import polyrun.sampler.HitAndRun;
import polyrun.thinning.ThinningFunction;
import vfeqs.model.solution.VFRankingSolution;

public class RandomRanking {
    public static int[] generate(VFProblem problem, double minEps, ThinningFunction thinningFunction) {
        int[] ranks = new int[problem.getNumberOfAlternatives()];

        VFModel model = problem.getModel(minEps);

        try {
            double[] sample = new SamplerRunner(new HitAndRun(thinningFunction)).sample(new ConstraintsSystem(model.getConstraints()), 1)[0];
            VFRankingSolution solution = new VFRankingSolution(sample, problem, 1e-10);
            for (int i = 0; i < problem.getNumberOfAlternatives(); i++) {
                ranks[i] = solution.getRank(i);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ranks;
    }
}
