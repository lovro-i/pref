package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.loader.SampleLoader;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.AMPxSSampler;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.File;

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
    return sampler;
  }

  
  public static void main(String[] args) throws Exception {
    SampleLoader loader = new SampleLoader(true, true, false, true, "-\t");
    RankingSample sample = loader.loadSample(new File("C:\\Projects\\Rank\\Papers\\prefaggregation\\Mallows_Model\\Experiments.Rank\\AMPxVariantsInputData\\1\\itemSetSize_50_sampleSize_2500_phi_08_miss_07.txt"));
    
    
    ItemSet items = sample.getItemSet();
    System.out.println(sample.size());
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.8);
    // RankingSample sample = MallowsUtils.sample(model, 2500);
    double miss = 0.7;
    // Filter.removeItems(sample, miss);
    
    
    MallowsModel initial = new MallowsModel(model.getCenter(), 0d);
    Logger.info("[Reconstructing phi = %.1f from %.1f, miss = %.1f]", model.getPhi(), initial.getPhi(), miss);
    
    int iters = 50;
    double threshold = 0.0001;
    
    {
      AMPxSReconstructor rec = new AMPxSReconstructor(initial, iters, 0.1);
      rec.setThreshold(threshold);
      long start = System.currentTimeMillis();
      MallowsModel reconstructed = rec.reconstruct(sample);
      Logger.info("AMPxS: phi = %f in %d ms", reconstructed.getPhi(), System.currentTimeMillis() - start);
    }
    
    {
      AMPxIReconstructor rec = new AMPxIReconstructor(initial, iters, 0.1);
      rec.setThreshold(threshold);
      long start = System.currentTimeMillis();
      MallowsModel reconstructed = rec.reconstruct(sample);
      Logger.info("AMPxI: phi = %f in %d ms", reconstructed.getPhi(), System.currentTimeMillis() - start);
    }
    
    {
      AMPxDIReconstructor rec = new AMPxDIReconstructor(initial, iters, 0.1);
      rec.setThreshold(threshold);
      long start = System.currentTimeMillis();
      MallowsModel reconstructed = rec.reconstruct(sample);
      Logger.info("AMPxDI: phi = %f in %d ms", reconstructed.getPhi(), System.currentTimeMillis() - start);
    }
  }
}
