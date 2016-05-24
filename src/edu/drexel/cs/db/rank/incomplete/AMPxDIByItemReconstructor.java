package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.other.AMPxDSamplerByItem;

/**
 * Creates insertion triangle from the sample of the previous iteration (only)
 * and updates it through the iteration. The first iteration uses the initial
 * sample. Dynamic, iterative, no smoothing, by item
 */
@Deprecated
public class AMPxDIByItemReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxDIByItemReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return null;
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    return new AMPxDSamplerByItem(estimate, resample, alpha);
  }
}
