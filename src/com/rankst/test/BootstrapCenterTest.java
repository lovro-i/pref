
package com.rankst.test;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.kemeny.BubbleTableKemenizator;
import com.rankst.kemeny.KemenyCandidate;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.CompleteReconstructor;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import com.rankst.triangle.SampleTriangleByRow;


public class BootstrapCenterTest {

  public static void main(String[] args) {
    double sum1 = 0;
    double sum2 = 0;
    double sum3 = 0;
    int reps = 20;
    for (int i = 0; i < reps; i++) {
      ElementSet elements = new ElementSet(50);
      Ranking center = elements.getRandomRanking();
      MallowsTriangle triangle = new MallowsTriangle(center, 0.92);
      RIMRSampler sampler = new RIMRSampler(triangle);
      Sample sample = sampler.generate(150);

      
      Ranking candidate = KemenyCandidate.find(sample);
      BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
      
      Ranking c1 = kemenizator.kemenize(sample, candidate);
      double d1 = KendallTauRankingDistance.between(center, c1);
      sum1 += d1;
      System.out.print(d1 + "\t");

      
      SampleTriangle triangle1 = new SampleTriangle(c1, sample);
      RIMRSampler resampler1 = new RIMRSampler(triangle1);
      Sample resample1 = resampler1.generate(10000);
      Ranking c2 = kemenizator.kemenize(resample1, c1);
      double d2 = KendallTauRankingDistance.between(center, c2);
      sum2 += d2;
      System.out.println(d2 + "\t");      
      
//      SampleTriangleByRow triangle2 = new SampleTriangleByRow(c1, sample);
//      RIMRSampler resampler2 = new RIMRSampler(triangle2);
//      Sample resample2 = resampler2.generate(10000);
//      Ranking c3 = kemenizator.kemenize(resample2, c1);
//      double d3 = KendallTauRankingDistance.between(center, c3);
//      sum3 += d3;
//      System.out.println(d3);      
      
      
    }
    
    
    System.out.println("======================");
    System.out.println(sum1 / reps);
    System.out.println(sum2 / reps);
    System.out.println(sum3 / reps);
  }
}
