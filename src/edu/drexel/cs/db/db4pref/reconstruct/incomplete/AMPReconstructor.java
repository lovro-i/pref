package edu.drexel.cs.db.db4pref.reconstruct.incomplete;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.db4pref.sampler.AMPSampler;
import edu.drexel.cs.db.db4pref.sampler.MallowsPosteriorSampler;

/**
 * Uses simple AMP
 */
public class AMPReconstructor extends EMReconstructor {

  public AMPReconstructor(MallowsModel model, int iterations) {
    super(model, iterations);
  }

  @Override
  protected MallowsPosteriorSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPSampler(model);
  }

  @Override
  protected MallowsPosteriorSampler updateSampler(MallowsPosteriorSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    sampler.setModel(estimate);
    return sampler;
  }


}
