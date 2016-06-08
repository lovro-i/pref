package edu.drexel.cs.db.db4pref.reconstruct.incomplete;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.db4pref.sampler.AMPxDSampler;
import edu.drexel.cs.db.db4pref.sampler.AMPxSampler;
import edu.drexel.cs.db.db4pref.sampler.MallowsPosteriorSampler;
import edu.drexel.cs.db.db4pref.util.Logger;

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
    MallowsPosteriorSampler sampler = new AMPxDSampler(estimate, sample, alpha);
    for (int i = 0; i < iterations; i++) {
      double oldPhi = estimate.getPhi();
      if (listener instanceof OnIterationListenerHybrid) {
        ((OnIterationListenerHybrid) listener).onIterationStart(i, estimate, !ampxd);
      } else if (listener instanceof OnIterationListener) {
        ((OnIterationListener) listener).onIterationStart(i, estimate);
      }
      
      
        
      if (ampxd) { // AMPx-D
        sampler.setModel(estimate);
        ((AMPxDSampler) sampler).reset();
      } 
      else if (ampxdi) sampler = new AMPxDSampler(estimate, resample, alpha); // AMPx-DI
      else sampler = new AMPxSampler(estimate, resample, alpha); // AMPx-I
      
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener instanceof OnIterationListenerHybrid) {
        ((OnIterationListenerHybrid) listener).onIterationEnd(i, estimate, !ampxd);
      } else if (listener instanceof OnIterationListener) {
        ((OnIterationListener) listener).onIterationEnd(i, estimate);
      }
      
      double newPhi = estimate.getPhi();
      if (Math.abs(newPhi - oldPhi) < threshold) break;
      
      // check if should switch to AMPxI
      if (ampxd) {
        double d = Math.signum(newPhi - oldPhi);
        if (direction == null) direction = d;
        else if (direction != d) {
          ampxd = false;
          resample = sample;
          estimate = new MallowsModel(center, oldPhi);
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
  protected MallowsPosteriorSampler initSampler(Sample<? extends PreferenceSet> sample) {
    throw new UnsupportedOperationException("Should never be called");
  }

  @Override
  protected MallowsPosteriorSampler updateSampler(MallowsPosteriorSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    throw new UnsupportedOperationException("Should never be called");
  }
  
  public static interface OnIterationListenerHybrid extends OnIterationListener {
    public void onIterationStart(int iteration, MallowsModel estimate, boolean isSwitched);
    public void onIterationEnd(int iteration, MallowsModel estimate, boolean isSwitched);
  }
   
}
