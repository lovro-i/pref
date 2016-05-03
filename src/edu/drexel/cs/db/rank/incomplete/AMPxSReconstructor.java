package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.AMPxSSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;


/** AMPxS
 */
public class AMPxSReconstructor extends EMReconstructor {

  private final double alpha;
  private boolean ampDiscard = false;

  public AMPxSReconstructor(MallowsModel model, int iterations, double alpha) {
    super(model, iterations);
    this.alpha = alpha;
  }
  
  /** Don't calculate AMP if it's share is less then 1‰
   * @param ampThreshold false to always calculate AMP
   */
  public void setDiscardSmallAmp(boolean ampDiscard) {
    this.ampDiscard = ampDiscard;
  }

  @Override
  protected MallowsSampler initSampler(Sample<? extends PreferenceSet> sample) {
    AMPxSSampler sampler = new AMPxSSampler(model, sample, alpha);
    sampler.setDiscardSmallAmp(ampDiscard);
    return sampler;
  }

  @Override
  protected MallowsSampler updateSampler(MallowsSampler sampler, MallowsModel estimate, Sample<? extends PreferenceSet> sample, Sample<? extends PreferenceSet> resample) {
    sampler.setModel(estimate);
    return sampler;
  }

  
  public static void main(String[] args) throws Exception {
//    SampleLoader loader = new SampleLoader(true, true, false, true, "-\t");
//    RankingSample sample = loader.loadSample(new File("C:\\Projects\\Rank\\Papers\\prefaggregation\\Mallows_Model\\Experiments.Rank\\AMPxVariantsInputData\\1\\itemSetSize_50_sampleSize_2500_phi_08_miss_07.txt"));
//    ItemSet items = sample.getItemSet();
    
     ItemSet items = new ItemSet(50);
     MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.8);
     RankingSample sample = MallowsUtils.sample(model, 2500);
    
    double miss = 0.7;
    Filter.removeItems(sample, miss);
    
    MallowsModel initial = new MallowsModel(model.getCenter(), 0d);
    Logger.info("[Reconstructing phi = %.1f from %.1f, miss = %.1f]", model.getPhi(), initial.getPhi(), miss);
    
    int iters = 50;
    double threshold = -1;
    
    OnIterationListener listener = new OnIterationListener() {
      
      private long start;
      
      @Override
      public void onIterationStart(int iteration, MallowsModel estimate) {
        start = System.currentTimeMillis();
      }

      @Override
      public void onIterationEnd(int iteration, MallowsModel estimate) {
        Logger.info("Iteration %d: %d ms", iteration+1, System.currentTimeMillis() - start);
      }
    };
    
    
    {
      AMPxSReconstructor rec = new AMPxSReconstructor(initial, iters, 0.1);
      rec.setThreshold(threshold);
      // rec.setOnIterationListener(listener);
      long start = System.currentTimeMillis();
      MallowsModel reconstructed = rec.reconstruct(sample);
      Logger.info("AMPxS: phi = %f in %d ms\n\n\n", reconstructed.getPhi(), System.currentTimeMillis() - start);
    }
    
    {
      AMPxIReconstructor rec = new AMPxIReconstructor(initial, iters, 0.1);
      rec.setThreshold(threshold);
      // rec.setOnIterationListener(listener);
      long start = System.currentTimeMillis();
      MallowsModel reconstructed = rec.reconstruct(sample);
      Logger.info("AMPxI: phi = %f in %d ms", reconstructed.getPhi(), System.currentTimeMillis() - start);
    }
    
    {
      AMPxDIReconstructor rec = new AMPxDIReconstructor(initial, iters, 0.1);
      rec.setThreshold(threshold);
      // rec.setOnIterationListener(listener);
      long start = System.currentTimeMillis();
      MallowsModel reconstructed = rec.reconstruct(sample);
      Logger.info("AMPxDI: phi = %f in %d ms", reconstructed.getPhi(), System.currentTimeMillis() - start);
    }
  }
}
