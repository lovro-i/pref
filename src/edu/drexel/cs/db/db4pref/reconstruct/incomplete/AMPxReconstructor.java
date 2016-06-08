package edu.drexel.cs.db.db4pref.reconstruct.incomplete;

import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.sampler.AMPxSampler;
import edu.drexel.cs.db.db4pref.sampler.MallowsPosteriorSampler;

/**
 * Creates insertion triangle from the sample and uses the same one repeatedly
 * AMPx
 */
public class AMPxReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }


  @Override
  protected MallowsPosteriorSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxSampler(model, sample, alpha);
  }

  @Override
  protected MallowsPosteriorSampler updateSampler(MallowsPosteriorSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    sampler.setModel(estimate);
    // double t = Math.max(0, 0.2 * estimate.getPhi() - 0.05);
    // setThreshold(t);
    return sampler;
  }
    
    
}
