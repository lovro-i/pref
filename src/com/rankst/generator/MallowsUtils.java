package com.rankst.generator;

import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.mixture.MallowsMixtureModel;
import com.rankst.mixture.MallowsMixtureSampler;
import com.rankst.model.MallowsModel;
import com.rankst.triangle.MallowsTriangle;


public class MallowsUtils {
  
  
  public static Sample sample(MallowsModel model, int size) {
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(triangle);
    return sampler.generate(size);
  }
  
  
  public static Sample sample(Ranking center, double phi, int size) {
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    return sampler.generate(size);
  }
  
  
  public static Sample sample(MallowsMixtureModel model, int size) {
    MallowsMixtureSampler sampler = new MallowsMixtureSampler(model);
    return sampler.generate(size);
  }
  
}
