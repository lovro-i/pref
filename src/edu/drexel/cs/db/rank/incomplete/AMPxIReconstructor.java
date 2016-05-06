package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPxSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;

/**
 * Creates insertion triangle from the sample of the previous iteration (only).
 * The first iteration uses the initial sample. Iterative, no smoothing
 * AMPxI
 */
public class AMPxIReconstructor extends EMReconstructor {

  private final double alpha;

  public AMPxIReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    return null;
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    // double t = Math.max(0, 0.2 * estimate.getPhi() - 0.05);
    // setThreshold(t);
    return new AMPxSampler(estimate, resample, alpha);
  }
  
  
  public static void main(String[] args) throws Exception {
    double phi = 0.8;
    double initialPhi = 0d;
    double alpha = 0.1d;
    double miss = 0.7d;
    
    double sumErr = 0;
    double sumAbsErr = 0;
    double tests = 15;
    for (int i = 0; i < tests; i++) {
      ItemSet items = new ItemSet(20);
      MallowsModel model = new MallowsModel(items.getRandomRanking(), phi);
      RankingSample sample = MallowsUtils.sample(model, 1000);
      Filter.removeItems(sample, miss);

      MallowsModel initial = new MallowsModel(model.getCenter(), initialPhi);
      AMPxIReconstructor rec = new AMPxIReconstructor(initial, 20, alpha);
      rec.setThreshold(0.005);
      MallowsModel reconstructed = rec.reconstruct(sample);
      
      double de = reconstructed.getPhi() - phi;
      System.out.println(de);
      sumErr += de;
      sumAbsErr += Math.abs(de);
    }
    sumErr /= tests;
    sumAbsErr /= tests;
    System.out.println("Avg. error     : " + sumErr);
    System.out.println("Avg. abs. error: " + sumAbsErr);
  }
}
