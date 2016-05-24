package edu.drexel.cs.db.rank.reconstruct;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.PreferenceSet;


public interface MallowsReconstructor {
  
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample) throws Exception;
  
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample, Ranking center) throws Exception;

}
