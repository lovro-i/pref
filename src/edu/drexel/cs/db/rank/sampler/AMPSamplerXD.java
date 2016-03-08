package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;

/** The one that uses the whole sample for probabilities. */
public class AMPSamplerXD extends AMPSamplerX {

  /** Very low rate (close to zero) favors sample information.
   * High rate (close to positive infinity) favors AMP.
   * 
   * @param model
   * @param sample
   * @param rate 
   */
  public AMPSamplerXD(MallowsModel model, Sample sample, double rate) {
    super(model, sample, rate);
  }

  public RankingSample sample(PreferenceSet pref, int count) {
    RankingSample out = new RankingSample(pref.getItemSet());
    for (int i = 0; i < count; i++) {
      Ranking r = sample(pref);
      out.add(r);
      this.addTrainingSample(r, 1);
    }
    return out;
  }
  
  
  /** Create new sample with completions of the rankings in the input one */
  public RankingSample sample(Sample<? extends PreferenceSet> sample) {
    RankingSample out = new RankingSample(sample.getItemSet());
    for (Sample.PW pw: sample) {
      Ranking r = sample(pw.p);
      out.add(r, pw.w);
      this.addTrainingSample(r, pw.w);
    }
    return out;
  }
  
}
