package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;

/** Base class for all Mallows posterior samplers */
public abstract class MallowsSampler {

  protected MallowsModel model;
  
  public MallowsSampler(MallowsModel model) {
    setModel(model);
  }
  
  public void setModel(MallowsModel model) {
    this.model = model;
  }
  
  public MallowsModel getModel() {
    return model;
  }
  
  public abstract Ranking sample(PreferenceSet pref);
  
  
  public RankingSample sample(PreferenceSet pref, int count) {
    RankingSample out = new RankingSample(pref.getItemSet());
    for (int i = 0; i < count; i++) {
      Ranking r = sample(pref);
      out.add(r);
    }
    return out;
  }
  
  
  /** Create new sample with completions of the rankings in the input one */
  public RankingSample sample(Sample<? extends PreferenceSet> sample) {
    RankingSample out = new RankingSample(sample.getItemSet());
    for (Sample.PW pw: sample) {
      Ranking r = sample(pw.p);
      out.add(r, pw.w);
    }
    return out;
  }
  
}
