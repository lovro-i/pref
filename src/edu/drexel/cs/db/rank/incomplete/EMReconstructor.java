package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;


public abstract class EMReconstructor implements MallowsReconstructor {

  protected final MallowsModel model;
  protected int iterations;
  protected EMIncompleteReconstructor.OnIterationListener listener;
  
  public EMReconstructor(MallowsModel model, int iterations) {
    this.model = model;
    this.iterations = iterations;
  }
  
  public void setIterations(int iters) {
    this.iterations = iters;
  }
  
  public void setOnIterationListener(EMIncompleteReconstructor.OnIterationListener listener) {
    this.listener = listener;
  }
  
  @Override
  public MallowsModel reconstruct(Sample<Ranking> sample) throws Exception {
    return this.reconstruct(sample, model.getCenter());
  }
  
}
