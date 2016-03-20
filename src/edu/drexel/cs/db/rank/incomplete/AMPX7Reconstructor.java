package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPSamplerXDItem;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;

/**
 * Constantly updates training sample after each ranking, adding to the same triangle all the time, during iterations, By Item
 * dynamic, smoothing, iterative, by item
 */
public class AMPX7Reconstructor extends EMReconstructor {

  private final double alpha;

  public AMPX7Reconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  public MallowsModel reconstruct(Sample<Ranking> sample, Ranking center) throws Exception {
    MallowsModel estimate = model;
    AMPSamplerXDItem sampler = new AMPSamplerXDItem(estimate, sample, alpha);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    Sample<Ranking> resample = sample;
    double oldPhi, newPhi;
    for (int i = 0; i < iterations; i++) {
      oldPhi = estimate.getPhi();
      if (listener != null) {
        listener.onIterationStart(i, estimate, sample);
      }
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener != null) {
        listener.onIterationEnd(i, estimate, resample);
      }
      newPhi = estimate.getPhi();
      if (Math.abs(newPhi - oldPhi) < 0.001) {
        break;
      }
    }

    return estimate;
  }

  public static void main(String[] args) throws Exception {
    ItemSet items = new ItemSet(10);
    Ranking ref = items.getReferenceRanking();
    RankingSample sample = MallowsUtils.sample(ref, 0.2, 10);
    Filter.remove(sample, 0.3);

    double initialPhi = 0.9;
    MallowsModel initial = new MallowsModel(ref, initialPhi);

    {
      long start = System.currentTimeMillis();
      EMReconstructor rec = new AMPX7Reconstructor(initial, 4, 1);
      MallowsModel model = rec.reconstruct(sample, ref);
      System.out.println("model = " + model);
      Logger.info("%s Done in %d ms", rec.getClass().getSimpleName(), System.currentTimeMillis() - start);
    }
  }

}
