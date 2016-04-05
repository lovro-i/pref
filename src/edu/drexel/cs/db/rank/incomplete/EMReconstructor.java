package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;

public abstract class EMReconstructor implements MallowsReconstructor {

  protected final MallowsModel model;
  
  /** Max number of iterations */
  protected int iterations;
  
  /** What to do on every iteration */
  protected OnIterationListener listener;
  
  /** Convergence threshold */
  protected double threshold = 0.001;

  
  public EMReconstructor(MallowsModel model, int iterations) {
    this.model = model;
    this.iterations = iterations;
  }

  /** Set maximum number of iterations */
  public void setIterations(int iters) {
    this.iterations = iters;
  }
  
  /** Stop if difference between two consecutive phis is less than threshold */
  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  
  public static interface OnIterationListener {
    public void onIterationStart(int iteration, MallowsModel estimate, Sample trainingSample);
    public void onIterationEnd(int iteration, MallowsModel estimate, Sample resample);
  }
  
  public void setOnIterationListener(OnIterationListener listener) {
    this.listener = listener;
  }
  
  @Override
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample) throws Exception {
    return this.reconstruct(sample, model.getCenter());
  }

  
  protected abstract MallowsSampler initSampler(Sample<? extends PreferenceSet> sample);
  
  protected abstract MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample);
  
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample, Ranking center) {
    MallowsModel estimate = model;
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    Sample resample = sample;
    MallowsSampler sampler = initSampler(sample);
    for (int i = 0; i < iterations; i++) {
      double oldPhi = estimate.getPhi();
      if (listener != null) listener.onIterationStart(i, estimate, sample);
      sampler = updateSampler(sampler, estimate, sample, resample);
      resample = sampler.sample(sample);
      estimate = reconstructor.reconstruct(resample, center);
      if (listener != null) listener.onIterationEnd(i, estimate, resample);
      double newPhi = estimate.getPhi();
      if (Math.abs(newPhi - oldPhi) < threshold) break;
    }

    return estimate;
  }
  
}
