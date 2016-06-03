package edu.drexel.cs.db.db4pref.triangle;

import edu.drexel.cs.db.db4pref.distance.KendallTauDistance;
import edu.drexel.cs.db.db4pref.distance.RankingDistance;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;

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

  
  @Override
  public TriangleRow getRow(int item) {
    double[] p = new double[item + 1];
    for (int i = 0; i < p.length; i++) {
      p[i] = get(item, i);      
    }
    return new TriangleRow(p);
  }
  
  
  
  /** One-based position, in order to be consistent with the paper
   * @param i Position
   * @param k Item
   * @return Probability that item k will be inserted to position i */
  public double w(int i, int k) {
    return Math.pow(phi, k-i) * (1 - phi) / (1 - Math.pow(phi, k));
  }

  public double get(int item, int pos) {
    return w(pos+1, item+1);
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
    
    TriangleRow row = triangle.getRow(1);
    System.out.println(row);
    
    
//    RIMRSampler sampler = new RIMRSampler(triangle);
//    Sample sample = sampler.generate(100000);
    
        
  }
  
  
}
