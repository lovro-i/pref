package edu.drexel.cs.db.rank.reconstruct;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;


public interface MallowsReconstructor {
  
  public MallowsModel reconstruct(Sample sample) throws Exception;
  
  public MallowsModel reconstruct(Sample sample, Ranking center) throws Exception;

}
