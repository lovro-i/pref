package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.sampler.AMPSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sampler.AMPSamplerPlus;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;


public class EMIncompleteReconstructor implements MallowsReconstructor {

  private int iterations = 10;
  private double startPhi = 0.5;
  
  private double rate;
  
  /** Construct EM reconstructor with rate between model and sample information.
   * If zero or negative, only model is used
   * otherwise, sample is weighted n / (rate + n)
   * @param rate How much is information from the sample favored: n / (rate + n)
   */
  public EMIncompleteReconstructor(double rate) {
    this.rate = rate;
  }
  
  @Override
  public MallowsModel reconstruct(Sample sample) {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }
  
  public void setIterations(int iters) {
    this.iterations = iters;
  }
  
  public void setInitialPhi(double phi) {
    this.startPhi = phi;
  }

  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) {
    ItemSet items = sample.getItemSet();
    MallowsModel estimate = new MallowsModel(center, startPhi);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    for (int i = 0; i < iterations; i++) {
      Sample resample = new Sample(items);
      AMPSampler amp = rate > 0 ? new AMPSamplerPlus(estimate, sample, rate) : new AMPSampler(estimate);
      for (int j = 0; j < sample.size(); j++) {
        Ranking r = sample.get(j).r;
        double w = sample.getWeight(j);
        if (r.size() == items.size()) {
          resample.add(r, w);
          continue;
        }
        
        Ranking complete = amp.sample(r);
        resample.add(complete, w);
      }
      estimate = reconstructor.reconstruct(resample, center);
      Logger.info(estimate);
    }
    
    return estimate;
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking ref = items.getReferenceRanking();
    Sample sample = MallowsUtils.sample(ref, 0.2, 5000);
    Filter.remove(sample, 0.4);
    
    {
      long start = System.currentTimeMillis();
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(0);
      rec.setInitialPhi(0.9);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("Done in %d ms", System.currentTimeMillis() - start);
    }
    
    {
      long start = System.currentTimeMillis();
      System.out.println();
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(5);
      rec.setInitialPhi(0.9);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("Done in %d ms", System.currentTimeMillis() - start);
    }
    
  }
}
