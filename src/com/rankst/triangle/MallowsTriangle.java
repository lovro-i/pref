package com.rankst.triangle;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.distance.RankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.model.MallowsModel;
import com.rankst.histogram.Histogram;
import com.rankst.histogram.HistogramReport;


public class MallowsTriangle extends Triangle {

  private double phi;
  
  public MallowsTriangle(MallowsModel model) {
    super(model.getCenter());
    this.phi = model.getPhi();
  }
  
  public MallowsTriangle(Ranking reference, double phi) {
    super(reference);
    this.phi = phi;
  }

  @Override
  public int randomPosition(int e) {
    double p = SampleTriangle.random.nextDouble();
    double s = 0;
    for (int i = 0; i < e; i++) {
      s += w(i+1, e+1);
      if (s > p) return i;
    }
    return e;
  }

   
  /** One-based position, in order to be consistent with the paper */
  public double w(int i, int k) {
    return Math.pow(phi, k-i) * (1 - phi) / (1 - Math.pow(phi, k));
  }

  @Override
  public String toString() {
    return "MallowsTriangle{" + "phi=" + phi + '}';
  }    

  public static void main(String[] args) {
    int n = 10;
    ElementSet elements = new ElementSet(n);
    double phi = 0.3;
    RankingDistance dist = new KendallTauRankingDistance();
    
    Ranking center = elements.getReferenceRanking();
    
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(100000);
    
        
  }
}
