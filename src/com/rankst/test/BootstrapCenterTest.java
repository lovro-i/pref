
package com.rankst.test;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.CompleteReconstructor;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;


public class BootstrapCenterTest {

  public static void main(String[] args) {
    double sumd = 0;
    double sumb = 0;
    int reps = 20;
    for (int i = 0; i < reps; i++) {
      ElementSet elements = new ElementSet(50);
      Ranking center = elements.getRandomRanking();
      System.out.println(center);
      MallowsTriangle triangle = new MallowsTriangle(center, 0.9);
      RIMRSampler sampler = new RIMRSampler(triangle);
      Sample sample = sampler.generate(200);

      
      CompleteReconstructor rec = new CompleteReconstructor();
      MallowsModel model = rec.reconstruct(sample);
      double d = KendallTauRankingDistance.between(center, model.getCenter());
      sumd += d;
      System.out.println(d);
      System.out.println("phi = " + model.getPhi());
      
      
      SampleTriangle striangle = new SampleTriangle(center, sample);
      RIMRSampler resampler = new RIMRSampler(striangle);
      Sample resample = resampler.generate(10000);
      MallowsModel m2 = rec.reconstruct(resample);
      double db = KendallTauRankingDistance.between(center, m2.getCenter());
      sumb += db;
      System.out.println(db);
      System.out.println("phi = " + m2.getPhi());
      
    }
    
    
    System.out.println("======================");
    System.out.println(sumd / reps);
    System.out.println(sumb / reps);
  }
}
