package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.triangle.Insertions;

/** AMPx variant that immediately updates the Insertion Probability Matrix with a newly sampled ranking */
public class AMPxSSampler extends AMPxSampler {

  private final Insertions[] insertions;
  
  /** Very low rate (close to zero) favors sample information.
   * High rate (close to positive infinity) favors AMP.
   * 
   * @param model
   * @param sample
   * @param alpha 
   */
  public AMPxSSampler(MallowsModel model, Sample<? extends PreferenceSet> sample, double alpha) {
    super(model, null, alpha);
    insertions = new Insertions[sample.size()];
    int idx = 0;
    for (PW<? extends PreferenceSet> pw: sample) {
      Insertions ins = new Insertions(pw.p, model.getCenter());
      insertions[idx++] = ins;
      triangle.add(ins, pw.w);
    }
  }
  
  public RankingSample sample(PreferenceSet pref, int count) {
    throw new IllegalArgumentException("Not supported");
  }
  
  /** Create new sample with completions of the rankings in the input one */
  public RankingSample sample(Sample<? extends PreferenceSet> sample) {
    if (sample.size() != insertions.length) throw new IllegalStateException("Sample sizes do not match");
    
    RankingSample out = new RankingSample(sample.getItemSet());
    for (int i = 0; i < insertions.length; i++) {
      PW<? extends PreferenceSet> pw = sample.get(i);
      Ranking r = sample(pw.p);
      out.add(r, pw.w);
      
      Insertions prev = insertions[i];
      triangle.sub(prev, pw.w);
      Insertions next = new Insertions(r, model.getCenter());
      triangle.add(next, pw.w);
      insertions[i] = next;
    }

    return out;
  }
  
}
