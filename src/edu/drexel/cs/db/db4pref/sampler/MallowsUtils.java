package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.db4pref.mixture.MallowsMixtureSampler;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.triangle.MallowsTriangle;


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
