package edu.drexel.cs.db.db4pref.reconstruct.incomplete;

import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.sampler.AMPxDSampler;
import edu.drexel.cs.db.db4pref.sampler.MallowsPosteriorSampler;

/**

/** Creates insertion triangle from the starting sample at the beginning of each iteration, and updates it through the iteration (after each ranking) 
 * Dynamic, no smoothing, not iterative
 * AMPxD
 */
public class AMPxDReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxDReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  protected MallowsPosteriorSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxDSampler(model, sample, alpha);
  }

  @Override
  protected MallowsPosteriorSampler updateSampler(MallowsPosteriorSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    sampler.setModel(estimate);
    ((AMPxDSampler) sampler).reset();
    return sampler;
  }

}
