package edu.drexel.cs.db.db4pref.reconstruct.incomplete;

import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.sampler.AMPxDSampler;
import edu.drexel.cs.db.db4pref.sampler.MallowsPosteriorSampler;

/**
 * Starts from the sample from the previous iteration, and updates it during
 * iteration dynamic, iterative, no smoothing
 * AMPxDI
 */
public class AMPxDIReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxDIReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }


  @Override
  protected MallowsPosteriorSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return null;
  }

  @Override
  protected MallowsPosteriorSampler updateSampler(MallowsPosteriorSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    return new AMPxDSampler(estimate, resample, alpha);
  }

  
}
