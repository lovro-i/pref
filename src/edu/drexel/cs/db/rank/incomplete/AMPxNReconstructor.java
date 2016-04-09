package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.AMPxNSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;

/**
 * Creates insertion triangle from the sample and uses the same one repeatedly
 * AMPx
 */
public class AMPxNReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxNReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }


  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxNSampler(model, sample, alpha);
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    sampler.setModel(estimate);
    return sampler;
  }

}
