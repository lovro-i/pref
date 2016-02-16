package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;


public interface MallowsSampler {

  public void setModel(MallowsModel model);
  
  public MallowsModel getModel();
  
  public Ranking sample(PreferenceSet pref);
  
  public Sample<Ranking> sample(Sample<? extends PreferenceSet> sample);
  
}
