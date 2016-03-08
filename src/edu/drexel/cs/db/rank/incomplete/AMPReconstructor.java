package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPSampler;

/** Uses simple AMP */
public class AMPReconstructor extends EMReconstructor {

  public AMPReconstructor(MallowsModel model, int iterations) {
    super(model, iterations);
  }


  @Override
  public MallowsModel reconstruct(Sample<Ranking> sample, Ranking center) {
    MallowsModel estimate = model;
    AMPSampler sampler = new AMPSampler(estimate);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    Sample<Ranking> resample = sample;
    for (int i = 0; i < iterations; i++) {
      sampler.setModel(estimate);
      if (listener != null) listener.onIterationStart(i, estimate, sample);
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener != null) listener.onIterationEnd(i, estimate, resample);
    }
    
    return estimate;
  }

}
