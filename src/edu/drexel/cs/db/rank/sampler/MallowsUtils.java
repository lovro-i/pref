package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;


public class MallowsUtils {
  
  public static Ranking sample(MallowsModel model) {
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(triangle);
    return sampler.generate();
  }
  
  public static RankingSample sample(MallowsModel model, int size) {
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(triangle);
    return sampler.generate(size);
  }
  
  
  public static RankingSample sample(Ranking center, double phi, int size) {
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    return sampler.generate(size);
  }
  
  
  public static RankingSample sample(MallowsMixtureModel model, int size) {
    MallowsMixtureSampler sampler = new MallowsMixtureSampler(model);
    return sampler.generate(size);
  }
  
}
