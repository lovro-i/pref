package com.rankst.test;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.filter.Filter;
import com.rankst.generator.RIMRSampler;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.CompleteReconstructor;
import com.rankst.triangle.MallowsTriangle;


public class NoiseTest {

  public static void main(String[] args) {
    ElementSet elements = new ElementSet(45);
    Ranking center = elements.getRandomRanking();
    
    double phi = 0.4;
    int sampleSize = 5000;
    
        
    MallowsModel original = new MallowsModel(center, phi);
    System.out.println(original);
    MallowsTriangle triangle = new MallowsTriangle(original);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    
    
    
    CompleteReconstructor direct = new CompleteReconstructor();
    
    MallowsModel mallows = direct.reconstruct(sample);
    System.out.println(mallows);
    System.out.println(KendallTauRankingDistance.between(center, mallows.getCenter()));
    
    Filter.swap(sample, 0.25);
    MallowsModel mallows1 = direct.reconstruct(sample);
    System.out.println(mallows1);
    System.out.println(KendallTauRankingDistance.between(center, mallows1.getCenter()));
  }
}
