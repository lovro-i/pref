package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPxDSampler;
import edu.drexel.cs.db.rank.sampler.AMPxSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.util.Logger;

/** Hybrid EM Reconstructor that first uses AMPxD, and when it stops converging, switches to AMPxI */
public class HybridReconstructor extends EMReconstructor {

  private final double alpha;
  
  public HybridReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample, Ranking center) {
    MallowsModel estimate = model;
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    Sample resample = sample;
    Double direction = null;
    boolean ampxd = true;
    for (int i = 0; i < iterations; i++) {
      double oldPhi = estimate.getPhi();
      if (listener != null) listener.onIterationStart(i, estimate, sample);
      
      MallowsSampler sampler;      
      if (ampxd) sampler = new AMPxDSampler(estimate, sample, alpha);
      else sampler = new AMPxSampler(estimate, resample, alpha);
      
      
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener != null) listener.onIterationEnd(i, estimate, resample);
      
      double newPhi = estimate.getPhi();
      if (Math.abs(newPhi - oldPhi) < threshold) break;
      
      // check if should switch to AMPxI
      if (ampxd) {
        double d = Math.signum(newPhi - oldPhi);
        if (direction == null) direction = d;
        else if (direction != d) {
          ampxd = false;
          Logger.info("Switching to AMPxI after %d iterations", i);
        }
      }
    }

    return estimate;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    throw new UnsupportedOperationException("Should never be called");
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    throw new UnsupportedOperationException("Should never be called");
  }

}
