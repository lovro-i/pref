package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.AMPxDSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;

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
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxDSampler(model, sample, alpha);
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    sampler.setModel(estimate);
    ((AMPxDSampler) sampler).reset();
    return sampler;
  }

}
