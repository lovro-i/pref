package edu.drexel.cs.db.rank.reconstruct;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;


public interface MallowsReconstructor {
  
  public MallowsModel reconstruct(Sample<Ranking> sample) throws Exception;
  
  public MallowsModel reconstruct(Sample<Ranking> sample, Ranking center) throws Exception;

}
