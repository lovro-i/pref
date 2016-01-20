package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.distance.RankingDistance;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;

/** Insertion probabilities triangle from a Mallows Model */ 
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

   
  /** One-based position, in order to be consistent with the paper
   * @param i Position
   * @param k Item
   * @return Probability that item k will be inserted to position i */
  public double w(int i, int k) {
    return Math.pow(phi, k-i) * (1 - phi) / (1 - Math.pow(phi, k));
  }

  @Override
  public String toString() {
    return "MallowsTriangle{" + "phi=" + phi + '}';
  }    

  public static void main(String[] args) {
    int n = 10;
    ItemSet items = new ItemSet(n);
    double phi = 0.3;
    RankingDistance dist = new KendallTauDistance();
    
    Ranking center = items.getReferenceRanking();
    
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(100000);
    
        
  }
}
