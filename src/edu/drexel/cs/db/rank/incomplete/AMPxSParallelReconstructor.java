package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPxSParallelSampler;
import edu.drexel.cs.db.rank.sampler.AMPxSSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;

/**

/** AMPxS
 */
public class AMPxSParallelReconstructor extends EMReconstructor {

  private final double alpha;
  private final int threads;

  public AMPxSParallelReconstructor(MallowsModel model, int iterations, double alpha) {
    this(model, iterations, alpha, Runtime.getRuntime().availableProcessors());
  }
  
  public AMPxSParallelReconstructor(MallowsModel model, int iterations, double alpha, int threads) {
    super(model, iterations);
    this.alpha = alpha;
    this.threads = threads;
    Logger.info("Setting %d workers in parallel", threads);
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return new AMPxSParallelSampler(model, sample, alpha, threads);
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
    double phi = 0.7;
    double initialPhi = 0d;
    double alpha = 0.1d;
    double miss = 0.7d;
    
    double sumErr = 0;
    double sumAbsErr = 0;
    double tests = 10;
    long ts = 0;
    long tp = 0;
    for (int i = 0; i < tests; i++) {
      ItemSet items = new ItemSet(80);
      MallowsModel model = new MallowsModel(items.getRandomRanking(), phi);
      RankingSample sample = MallowsUtils.sample(model, 1000);
      Filter.removeItems(sample, miss);

      MallowsModel initial = new MallowsModel(model.getCenter(), initialPhi);
      
      {
        long start = System.currentTimeMillis();
        AMPxSReconstructor rec = new AMPxSReconstructor(initial, 20, alpha);
        rec.setThreshold(0.005);
        MallowsModel reconstructed = rec.reconstruct(sample);
        ts += System.currentTimeMillis() - start;
        double de = reconstructed.getPhi() - phi;
        System.out.println(de);
      }
      
      {
        long start = System.currentTimeMillis();
        AMPxSParallelReconstructor rec = new AMPxSParallelReconstructor(initial, 20, alpha);
        rec.setThreshold(0.005);
        MallowsModel reconstructed = rec.reconstruct(sample);
        tp += System.currentTimeMillis() - start;
        double de = reconstructed.getPhi() - phi;
        System.out.println(de);
      }
      
      System.out.println();
    }
    sumErr /= tests;
    sumAbsErr /= tests;
    // System.out.println("Avg. error     : " + sumErr);
    // System.out.println("Avg. abs. error: " + sumAbsErr);
    
    Logger.info("Sequential: %d ms", ts);
    Logger.info("Parallel: %d ms", tp);
    Logger.info("Speedup: %.1f x", 1d * ts / tp);
  }

}
