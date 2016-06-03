package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.incomplete.EMReconstructor.OnIterationListener;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPSampler;
import edu.drexel.cs.db.rank.sampler.AMPxSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;

@Deprecated
public class EMIncompleteReconstructor implements MallowsReconstructor {

  private int iterations = 100;

  private MallowsSampler sampler;
  private OnIterationListener listener;

  /**
   * Construct EM reconstructor with specified sampler
   */
  public EMIncompleteReconstructor(MallowsSampler sampler) {
    this.sampler = sampler;
  }

  @Override
  public MallowsModel reconstruct(Sample sample) {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }

  public void setIterations(int iters) {
    this.iterations = iters;
  }

  public void setOnIterationListener(OnIterationListener listener) {
    this.listener = listener;
  }
  
  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) {
    MallowsModel estimate = sampler.getModel();
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    Sample resample = sample;
    double oldPhi, newPhi;
    for (int i = 0; i < iterations; i++) {
      oldPhi = estimate.getPhi();
      sampler.setModel(estimate);
      if (listener != null) {
        listener.onIterationStart(i, estimate);
      }
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener != null) {
        listener.onIterationEnd(i, estimate);
      }
      newPhi = estimate.getPhi();
      if (Math.abs(newPhi - oldPhi) < 0.001) {
        break;
      }
    }

    return estimate;
  }

  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking ref = items.getReferenceRanking();
    RankingSample sample = MallowsUtils.sample(ref, 0.2, 1000);
    Filter.removeItems(sample, 0.3);

    OnIterationListener listener = new OnIterationListener() {
      @Override
      public void onIterationStart(int iteration, MallowsModel estimate) {
      }

      @Override
      public void onIterationEnd(int iteration, MallowsModel estimate) {
        Logger.info("Iteration %d: %f", iteration, estimate.getPhi());
      }

    };

    double initialPhi = 0.9;
    MallowsModel initial = new MallowsModel(ref, initialPhi);

    {
      long start = System.currentTimeMillis();
      MallowsSampler sampler = new AMPSampler(initial);
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(sampler);
      rec.setIterations(4);
      rec.setOnIterationListener(listener);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("%s Done in %d ms", sampler.getClass().getSimpleName(), System.currentTimeMillis() - start);
    }

    {
      long start = System.currentTimeMillis();
      MallowsSampler sampler = new AMPxSampler(initial, sample, 10);
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(sampler);
      rec.setIterations(4);
      rec.setOnIterationListener(listener);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("%s Done in %d ms", sampler.getClass().getSimpleName(), System.currentTimeMillis() - start);
    }

  }
}
