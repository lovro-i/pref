
package com.rankst.test;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.histogram.Histogram;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.CompleteReconstructor;
import com.rankst.triangle.MallowsTriangle;


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
    ElementSet elements = new ElementSet(15);
    Ranking reference = elements.getReferenceRanking();
    
    double phi = 0.3;
    int sampleSize = 5000;
    
    MallowsTriangle triangle = new MallowsTriangle(reference, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize); 
    
    CompleteReconstructor direct = new CompleteReconstructor();
    MallowsModel mallows = direct.reconstruct(sample);
    // System.out.println(mallows);
    
    Histogram<Integer> hist = new Histogram<Integer>();
    for (Ranking r: sample) {
      int d = (int) KendallTauRankingDistance.getInstance().distance(reference, r);
      hist.add(d, 1);
    }
    
    //System.out.println(z(elements.size(), phi));
    System.out.println("----");
    System.out.println(hist.get(0));
    System.out.println(hist.get(1));
    System.out.println(hist.get(2));
  }
}
