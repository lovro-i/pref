package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.AMPxSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;

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
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxSampler(model, sample, alpha);
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    sampler.setModel(estimate);
    // double t = Math.max(0, 0.2 * estimate.getPhi() - 0.05);
    // setThreshold(t);
    return sampler;
  }
    
    
}
