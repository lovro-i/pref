package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.db4pref.mixture.MallowsMixtureSampler;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.sampler.triangle.MallowsTriangle;


public class MallowsUtils {
  
  public static Ranking sample(MallowsModel model) {
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMSampler sampler = new RIMSampler(triangle);
    return sampler.sample();
  }
  
  public static RankingSample sample(MallowsModel model, int size) {
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMSampler sampler = new RIMSampler(triangle);
    return sampler.sample(size);
  }
  
  
  public static RankingSample sample(Ranking center, double phi, int size) {
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMSampler sampler = new RIMSampler(triangle);
    return sampler.sample(size);
  }
  
  
  public static RankingSample sample(MallowsMixtureModel model, int size) {
    MallowsMixtureSampler sampler = new MallowsMixtureSampler(model);
    return sampler.sample(size);
  }
  
}
