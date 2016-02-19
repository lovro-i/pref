package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPGSampler;
import edu.drexel.cs.db.rank.sampler.AMPSampler;
import edu.drexel.cs.db.rank.sampler.AMPSamplerPlus;
import edu.drexel.cs.db.rank.sampler.AMPSamplerPlusPlus;
import edu.drexel.cs.db.rank.sampler.AMPSamplerX;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;



public class EMIncompleteReconstructor implements MallowsReconstructor {

  private int iterations = 10;
  
  private MallowsSampler sampler;
  private OnIterationListener listener;
  
  /** Construct EM reconstructor with specified sampler
   */
  public EMIncompleteReconstructor(MallowsSampler sampler) {
    this.sampler = sampler;
  }
  

  @Override
  public MallowsModel reconstruct(Sample<Ranking> sample) {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }
  
  public void setIterations(int iters) {
    this.iterations = iters;
  }
  
  public void setOnIterationListener(OnIterationListener listener) {
    this.listener = listener;
  }
  
 
  @Override
  public MallowsModel reconstruct(Sample<Ranking> sample, Ranking center) {
    MallowsModel estimate = sampler.getModel();
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

  
  public static interface OnIterationListener {
    
    public void onIterationStart(int iteration, MallowsModel estimate, Sample<Ranking> trainingSample);
    public void onIterationEnd(int iteration, MallowsModel estimate, Sample<Ranking> resample);
  }
  
  
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking ref = items.getReferenceRanking();
    RankingSample sample = MallowsUtils.sample(ref, 0.2, 1000);
    Filter.remove(sample, 0.3);
    
    
    OnIterationListener listener = new OnIterationListener() {
      @Override
      public void onIterationStart(int iteration, MallowsModel estimate, Sample<Ranking> trainingSample) {        
      }

      @Override
      public void onIterationEnd(int iteration, MallowsModel estimate, Sample<Ranking> resample) {
        Logger.info("Iteration %d: %f", iteration, estimate.getPhi());
      }
      
    };
    
    
    double initialPhi = 0.9;
    MallowsModel initial = new MallowsModel(ref, initialPhi);
    
    
    {
      long start = System.currentTimeMillis();
      MallowsSampler sampler = new AMPSampler(initial);
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(sampler);
      rec.setIterations(4);
      rec.setOnIterationListener(listener);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("%s Done in %d ms", sampler.getClass().getSimpleName(), System.currentTimeMillis() - start);
    }
    
    {
      long start = System.currentTimeMillis();
      MallowsSampler sampler = new AMPSamplerX(initial, sample, 10);
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(sampler);
      rec.setIterations(4);
      rec.setOnIterationListener(listener);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("%s Done in %d ms", sampler.getClass().getSimpleName(), System.currentTimeMillis() - start);
    }
    
    {
      long start = System.currentTimeMillis();
      MallowsSampler sampler = new AMPSamplerPlus(initial, sample, 10);
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(sampler);
      rec.setIterations(4);
      rec.setOnIterationListener(listener);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("%s Done in %d ms", sampler.getClass().getSimpleName(), System.currentTimeMillis() - start);
    }
    
    {
      long start = System.currentTimeMillis();
      MallowsSampler sampler = new AMPSamplerPlusPlus(initial, sample, 10);
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(sampler);
      rec.setIterations(4);
      rec.setOnIterationListener(listener);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("%s Done in %d ms", sampler.getClass().getSimpleName(), System.currentTimeMillis() - start);
    }

    
  }
}
