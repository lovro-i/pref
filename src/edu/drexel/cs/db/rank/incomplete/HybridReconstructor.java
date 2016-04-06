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

/** Hybrid EM Reconstructor that first uses AMPxD, and when it stops converging, switches to AMPxI or AMPxDI (choose in constructor) */
public class HybridReconstructor extends EMReconstructor {

  private final double alpha;
  private final boolean ampxdi;
  
  /** Creates a hybrid reconstructor
   *
   * @param model Starting model for estimation
   * @param iterations Maximum number of iterations
   * @param alpha Split between AMP and sample information
   * @param ampxdi Sampler to switch to after AMPxD: true for AMPxDI, false for AMPxI
   */
  public HybridReconstructor(MallowsModel model, int iterations, double alpha, boolean ampxdi) {
    super(model, iterations);
    this.alpha = alpha;
    this.ampxdi = ampxdi;
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
      if (listener instanceof OnIterationListener) ((OnIterationListener) listener).onIterationStart(i, estimate);
      if (listener instanceof OnIterationListenerHybrid) ((OnIterationListenerHybrid) listener).onIterationStart(i, estimate, !ampxd);
      
      MallowsSampler sampler;      
      if (ampxd) sampler = new AMPxDSampler(estimate, sample, alpha);
      else if (ampxdi) sampler = new AMPxDSampler(estimate, resample, alpha);
      else sampler = new AMPxSampler(estimate, resample, alpha);
      
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener instanceof OnIterationListener) ((OnIterationListener) listener).onIterationEnd(i, estimate);
      if (listener instanceof OnIterationListenerHybrid) ((OnIterationListenerHybrid) listener).onIterationEnd(i, estimate, !ampxd);
      
      double newPhi = estimate.getPhi();
      if (Math.abs(newPhi - oldPhi) < threshold) break;
      
      // check if should switch to AMPxI
      if (ampxd) {
        double d = Math.signum(newPhi - oldPhi);
        if (direction == null) direction = d;
        else if (direction != d) {
          ampxd = false;
          if (ampxdi){
            Logger.info("Switching to AMPx-DI after %d iterations", i);
          } else {
            Logger.info("Switching to AMPx-I after %d iterations", i);
          }
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
  
  public static interface OnIterationListenerHybrid extends OnIterationListener {
    public void onIterationStart(int iteration, MallowsModel estimate, boolean isSwitched);
    public void onIterationEnd(int iteration, MallowsModel estimate, boolean isSwitched);
  }
   
}
