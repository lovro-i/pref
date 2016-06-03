package edu.drexel.cs.db.db4pref.incomplete;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.db4pref.sampler.AMPxSampler;
import edu.drexel.cs.db.db4pref.sampler.MallowsSampler;

/**
 * Creates insertion triangle from the starting sample, and updates it after
 * each iteration Iterative, with smoothing
 * REMOVE
 */
@Deprecated
public class AMPxISReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxISReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxSampler(model, null, alpha);
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    sampler.setModel(estimate);
    ((AMPxSampler) sampler).addTrainingSample(resample);
    return sampler;
  }

}
