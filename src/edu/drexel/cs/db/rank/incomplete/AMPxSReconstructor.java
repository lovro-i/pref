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
    ItemSet items = new ItemSet(20);
    MallowsModel model = new MallowsModel(items.getRandomRanking(), 0.2);
    RankingSample sample = MallowsUtils.sample(model, 1000);
    Filter.removeItems(sample, 0.5);
    
    MallowsModel initial = new MallowsModel(model.getCenter(), 0.9);
    AMPxSReconstructor rec = new AMPxSReconstructor(initial, 100, 0.1);
    MallowsModel reconstructed = rec.reconstruct(sample);
    System.out.println(reconstructed);
  }
}
