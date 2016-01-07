package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.distance.RankingDistance;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.histogram.Histogram;
import edu.drexel.cs.db.rank.histogram.HistogramReport;


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
    RankingDistance dist = new KendallTauDistance();
    
    Ranking center = elements.getReferenceRanking();
    
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(100000);
    
        
  }
}
