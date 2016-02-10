
package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.util.Histogram;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;


public class SomeTest1 {

  
  public static double z(int n, double phi) {
    double f = 1;
    double p = 1;
    double phipow = 1;
    for (int i = 0; i < n; i++) {
      phipow *= phi;
      f += phipow;
      System.out.println("f["+i+"] = "+ f);
      p *= f;
    }
    return p;
  } 
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(15);
    Ranking reference = items.getReferenceRanking();
    
    double phi = 0.3;
    int sampleSize = 5000;
    
    MallowsTriangle triangle = new MallowsTriangle(reference, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize); 
    
    CompleteReconstructor direct = new CompleteReconstructor();
    MallowsModel mallows = direct.reconstruct(sample);
    // System.out.println(mallows);
    
    Histogram<Integer> hist = new Histogram<Integer>();
    for (Ranking r: sample.rankings()) {
      int d = (int) KendallTauDistance.getInstance().distance(reference, r);
      hist.add(d, 1);
    }
    
    //System.out.println(z(items.size(), phi));
    System.out.println("----");
    System.out.println(hist.get(0));
    System.out.println(hist.get(1));
    System.out.println(hist.get(2));
  }
}
