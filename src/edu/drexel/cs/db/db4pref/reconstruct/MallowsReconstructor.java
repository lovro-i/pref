package edu.drexel.cs.db.db4pref.reconstruct;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;


public interface MallowsReconstructor {
  
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample) throws Exception;
  
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample, Ranking center) throws Exception;

}
