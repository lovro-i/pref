package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.AMPxCSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;

/**

/** Creates insertion triangle from the starting sample at the beginning of each iteration, and updates it through the iteration (after each ranking) 
 * Dynamic, no smoothing, not iterative
 * AMPxD
 */
public class AMPxCReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxCReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    AMPxCSampler sampler = new AMPxCSampler(model, alpha, sample, sample);
    sampler.setDynamic(0, true);
    return sampler;
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    AMPxCSampler samp = (AMPxCSampler) sampler;
    samp.setModel(estimate);
    samp.reset(0);
    samp.setTrainingSample(1, resample);
    return samp;
  }

}
