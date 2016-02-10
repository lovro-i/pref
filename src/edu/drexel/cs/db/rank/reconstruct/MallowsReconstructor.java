package edu.drexel.cs.db.rank.reconstruct;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;


public interface MallowsReconstructor {
  
  public MallowsModel reconstruct(Sample sample) throws Exception;
  
  public MallowsModel reconstruct(Sample sample, Ranking center) throws Exception;

}
