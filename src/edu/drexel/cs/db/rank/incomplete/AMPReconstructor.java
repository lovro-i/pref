package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPSampler;

/**
 * Uses simple AMP
 */
public class AMPReconstructor extends EMReconstructor {

  public AMPReconstructor(MallowsModel model, int iterations) {
    super(model, iterations);
  }

  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) {
    MallowsModel estimate = model;
    AMPSampler sampler = new AMPSampler(estimate);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    Sample resample = sample;
    double oldPhi, newPhi;
    for (int i = 0; i < iterations; i++) {
      oldPhi = estimate.getPhi();
      sampler.setModel(estimate);
      if (listener != null) {
        listener.onIterationStart(i, estimate, sample);
      }
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener != null) {
        listener.onIterationEnd(i, estimate, resample);
      }
      newPhi = estimate.getPhi();
      if (Math.abs(newPhi - oldPhi) < 0.001) {
        break;
      }
    }

    return estimate;
  }

}
