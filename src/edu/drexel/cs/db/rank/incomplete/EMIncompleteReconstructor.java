package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
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
  private final boolean useCompleteSample;
  private IterationListener listener;
  
  /** Construct EM reconstructor with rate between model and sample information.
   * If zero or negative, only model is used
   * otherwise, sample is weighted n / (rate + n)
   * @param rate How much is information from the sample favored: n / (rate + n)
   * @param useCompleteSample If true, it uses the completed sample from the previous iteration. If false, always uses the input sample
   */
  public EMIncompleteReconstructor(double rate, boolean useCompleteSample) {
    this.rate = rate;
    this.useCompleteSample = useCompleteSample;
  }
  
  /** Uses ordinary AMP */
  public EMIncompleteReconstructor() {
    this(0, false);
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

  public boolean isPlus() {
    return rate > 0;
  }
  
  public boolean isPlusPlus() {
    return (rate > 0) && useCompleteSample;
  }
  
  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) {
    ItemSet items = sample.getItemSet();
    MallowsModel estimate = new MallowsModel(center, startPhi);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    AMPSamplerPlus amp = new AMPSamplerPlus(estimate, rate);
    if (isPlus() && !isPlusPlus()) amp.setTrainingSample(sample);
    Sample resample = sample;
    for (int i = 0; i < iterations; i++) {
      if (isPlusPlus()) amp.setTrainingSample(resample);
      amp.setModel(estimate);
      if (listener != null) listener.onIterationStart(i, estimate, amp.getTrainingSample());
      
      resample = new Sample(items);
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
      if (listener != null) listener.onIterationEnd(i, estimate, resample);
    }
    
    return estimate;
  }

  
  public static interface IterationListener {
    
    public void onIterationStart(int iteration, MallowsModel estimate, Sample trainingSample);
    public void onIterationEnd(int iteration, MallowsModel estimate, Sample resample);
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking ref = items.getReferenceRanking();
    Sample sample = MallowsUtils.sample(ref, 0.9, 500);
    Filter.remove(sample, 0.4);
    
    {
      long start = System.currentTimeMillis();
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor();
      rec.setInitialPhi(0.2);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("Done in %d ms", System.currentTimeMillis() - start);
    }
    
    {
      long start = System.currentTimeMillis();
      System.out.println();
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(5, false);
      rec.setInitialPhi(0.2);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("Done in %d ms", System.currentTimeMillis() - start);
    }
    
    {
      long start = System.currentTimeMillis();
      System.out.println();
      EMIncompleteReconstructor rec = new EMIncompleteReconstructor(5, true);
      rec.setInitialPhi(0.2);
      MallowsModel model = rec.reconstruct(sample, ref);
      Logger.info("Done in %d ms", System.currentTimeMillis() - start);
    }
    
  }
}
