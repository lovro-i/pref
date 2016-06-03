package edu.drexel.cs.db.db4pref.incomplete;

import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.sampler.AMPxDSampler;
import edu.drexel.cs.db.db4pref.sampler.MallowsSampler;

/**
 * Constantly updates training sample after each ranking, adding to the same
 * triangle all the time, during iterations dynamic, smoothing (therefore
 * iterative)
 */
@Deprecated
public class AMPxDISReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxDISReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxDSampler(model, sample, alpha);
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    // double t = Math.max(0, 0.2 * estimate.getPhi() - 0.05);
    // setThreshold(t);
    return sampler;
  }

}
