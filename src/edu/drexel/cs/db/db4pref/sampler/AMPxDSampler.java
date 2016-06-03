package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.triangle.ConfidentTriangle;
import edu.drexel.cs.db.db4pref.util.Logger;

/** AMPx variant that immediately updates the Insertion Probability Matrix with a newly sampled ranking */
public class AMPxDSampler extends AMPxSampler {

  private ConfidentTriangle copy;
  
  /** Very low rate (close to zero) favors sample information.
   * High rate (close to positive infinity) favors AMP.
   * 
   * @param model
   * @param sample
   * @param rate 
   */
  public AMPxDSampler(MallowsModel model, Sample sample, double rate) {
    super(model, sample, rate);
    copy = triangle.clone();
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
  
  public void reset() {
    triangle.clone(copy);
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
