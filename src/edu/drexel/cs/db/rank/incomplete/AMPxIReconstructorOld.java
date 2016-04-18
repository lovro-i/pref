package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.AMPxSamplerOld;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;

/**
 * Creates insertion triangle from the sample of the previous iteration (only).
 * The first iteration uses the initial sample. Iterative, no smoothing
 * AMPxI
 */
public class AMPxIReconstructorOld extends EMReconstructor {

  private final double alpha;

  public AMPxIReconstructorOld(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return null;
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    return new AMPxSamplerOld(estimate, resample, alpha);
  }
}
