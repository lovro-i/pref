package edu.drexel.cs.db.rank.ppm;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;


public class PairwisePreferenceMatrix {

  private ElementSet elements;
  private double[][] ppm;
  
  
  public PairwisePreferenceMatrix(Sample sample) {
    this.elements = sample.getElements();
    int n = elements.size();
    ppm = new double[n][n];
    
    for (int ri = 0; ri < sample.size(); ri++) {
      Ranking r = sample.get(ri);
      double w = sample.getWeight(ri);
      for (int i = 0; i < r.size()-1; i++) {
        int e1 = r.get(i).getId();
        for (int j = i+1; j < r.size(); j++) {
          int e2 = r.get(j).getId();
          ppm[e1][e2] += w;
        }
      }
    }
  }
  
  public ElementSet getElements() {
    return elements;
  }
  
  public double getProbabilityBefore(int i, int j) {
    double sum = ppm[i][j] + ppm[j][i];
    if (sum == 0) return 0.5d;
    return ppm[i][j] / sum;
  }
  
}
