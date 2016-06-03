
package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.distance.KendallTauDistance;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.sampler.RIMRSampler;
import edu.drexel.cs.db.db4pref.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.db4pref.kemeny.KemenyCandidate;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.db4pref.triangle.MallowsTriangle;
import edu.drexel.cs.db.db4pref.triangle.SampleTriangle;
import edu.drexel.cs.db.db4pref.triangle.SampleTriangleByRow;


public class BootstrapCenterTest {

  public static void main(String[] args) {
    double sum1 = 0;
    double sum2 = 0;
    double sum3 = 0;
    int reps = 20;
    for (int i = 0; i < reps; i++) {
      ItemSet items = new ItemSet(50);
      Ranking center = items.getRandomRanking();
      MallowsTriangle triangle = new MallowsTriangle(center, 0.92);
      RIMRSampler sampler = new RIMRSampler(triangle);
      RankingSample sample = sampler.generate(150);

      
      Ranking candidate = KemenyCandidate.find(sample);
      BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
      
      Ranking c1 = kemenizator.kemenize(sample, candidate);
      double d1 = KendallTauDistance.between(center, c1);
      sum1 += d1;
      System.out.print(d1 + "\t");

      
      SampleTriangle triangle1 = new SampleTriangle(c1, sample);
      RIMRSampler resampler1 = new RIMRSampler(triangle1);
      RankingSample resample1 = resampler1.generate(10000);
      Ranking c2 = kemenizator.kemenize(resample1, c1);
      double d2 = KendallTauDistance.between(center, c2);
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
