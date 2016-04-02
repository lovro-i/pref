package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;

public abstract class EMReconstructor implements MallowsReconstructor {

  protected final MallowsModel model;
  protected int iterations;
  protected OnIterationListener listener;

  
  public EMReconstructor(MallowsModel model, int iterations) {
    this.model = model;
    this.iterations = iterations;
  }

  public void setIterations(int iters) {
    this.iterations = iters;
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

}
