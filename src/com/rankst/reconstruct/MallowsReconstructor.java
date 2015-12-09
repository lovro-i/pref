package com.rankst.reconstruct;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.model.MallowsModel;
import com.rankst.triangle.MallowsTriangle;


public interface MallowsReconstructor {
  
  public MallowsModel reconstruct(Sample sample) throws Exception;
  
  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    int n = 30;
    ElementSet elements = new ElementSet(n);
    
    Ranking center = elements.getRandomRanking();
    double phi = 0.3;
    MallowsModel originalModel = new MallowsModel(center, phi);
    System.out.println(originalModel);
    
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(1000000);
    
    MallowsModel model = new DirectReconstructor().reconstruct(sample);
    System.out.println(model);    
  }

}
