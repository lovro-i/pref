package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPSamplerXD;
import edu.drexel.cs.db.rank.sampler.AMPSamplerXDItem;

/** Constantly updates training sample after each ranking, adding to the same triangle all the time, during iterations */
public class AMPX7Reconstructor extends EMReconstructor {

  private final double alpha;
  
  public AMPX7Reconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  public MallowsModel reconstruct(Sample<Ranking> sample, Ranking center) throws Exception {
    MallowsModel estimate = model;
    AMPSamplerXDItem sampler = new AMPSamplerXDItem(estimate, sample, alpha);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    Sample<Ranking> resample = sample;
    for (int i = 0; i < iterations; i++) {
      if (listener != null) listener.onIterationStart(i, estimate, sample);
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener != null) listener.onIterationEnd(i, estimate, resample);
    }
    
    return estimate;
  }

}
