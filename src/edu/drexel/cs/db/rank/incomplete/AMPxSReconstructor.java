package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPxSSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;

/**

/** AMPxS
 */
public class AMPxSReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxSReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxSSampler(model, sample, alpha);
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    //MallowsModel nextModel = new MallowsModel(estimate.getCenter(), 0.9*estimate.getPhi());
    //sampler.setModel(nextModel);
    sampler.setModel(estimate);
    // double t = Math.max(0, 0.2 * estimate.getPhi() - 0.05);
    // setThreshold(t);
    return sampler;
  }

  
  public static void main(String[] args) throws Exception {
    double phi = 0.8;
    double initialPhi = 0d;
    double alpha = 0.1d;
    double miss = 0.7d;
    
    
    {
      long start = System.currentTimeMillis();
      ItemSet items = new ItemSet(170);
      MallowsModel model = new MallowsModel(items.getRandomRanking(), phi);
      RankingSample sample = MallowsUtils.sample(model, 1000);
      PolynomialReconstructor rec = new PolynomialReconstructor();
      MallowsModel m2 = rec.reconstruct(sample);
      Logger.info("Reconstructed %f in %d ms", m2.getPhi(), System.currentTimeMillis() - start);
    }
    
    
    long prev = 1;
    for (int its = 150; its <= 300; its += 10) {
      long start = System.currentTimeMillis();
      ItemSet items = new ItemSet(its);
      MallowsModel model = new MallowsModel(items.getRandomRanking(), phi);
      RankingSample sample = MallowsUtils.sample(model, 1000);
      Filter.removeItems(sample, miss);

      MallowsModel initial = new MallowsModel(model.getCenter(), initialPhi);
      AMPxIReconstructor rec = new AMPxIReconstructor(initial, 20, alpha);
      rec.setThreshold(0.005);
      MallowsModel reconstructed = rec.reconstruct(sample);
      
      long t = System.currentTimeMillis() - start;
      Logger.info("%d items done in %d ms (%.1fx)", its, t, 1d * t / prev);
      prev = t;
    }
    
    
    System.exit(0);
  
    ItemSet items = new ItemSet(50);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    RankingSample sample = MallowsUtils.sample(model, 2500);
    
    Filter.removeItems(sample, miss);
    
    MallowsModel initial = new MallowsModel(model.getCenter(), initialPhi);
    Logger.info("[Reconstructing phi = %.1f from %.1f, miss = %.1f]", model.getPhi(), initial.getPhi(), miss);
    
    int iters = 20;
    double threshold = -1;
    
    OnIterationListener listener = new OnIterationListener() {
      
      private long start;
      
      @Override
      public void onIterationStart(int iteration, MallowsModel estimate) {
        start = System.currentTimeMillis();
      }

      @Override
      public void onIterationEnd(int iteration, MallowsModel estimate) {
        Logger.info("Iteration %d: %d ms | phi = %f", iteration+1, System.currentTimeMillis() - start, estimate.getPhi());
      }
    };
    
    
    {
      AMPxSReconstructor rec = new AMPxSReconstructor(initial, iters, alpha);
      rec.setThreshold(threshold);
      rec.setOnIterationListener(listener);
      long start = System.currentTimeMillis();
      MallowsModel reconstructed = rec.reconstruct(sample);
      Logger.info("AMPxS: phi = %f in %d ms\n\n\n", reconstructed.getPhi(), System.currentTimeMillis() - start);
    }
    
  }
}
