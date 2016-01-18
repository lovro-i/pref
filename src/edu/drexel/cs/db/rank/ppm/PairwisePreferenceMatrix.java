package edu.drexel.cs.db.rank.ppm;

import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Ratings;
import edu.drexel.cs.db.rank.entity.RatingsSample;
import edu.drexel.cs.db.rank.entity.Sample;
import java.util.List;
import java.util.Map.Entry;


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
  
  public PairwisePreferenceMatrix(RatingsSample sample) {
    this.elements = sample.getElements();
    int n = elements.size();
    ppm = new double[n][n];
    
    for (Entry<Ratings, Double> entry: sample.entrySet()) {
    
      List<List<Element>> groups = entry.getKey().getGroups();
      for (int i = 0; i < groups.size()-1; i++) {
        for (Element e1: groups.get(i)) {
          int ie1 = e1.getId();
          for (int j = i+1; j < groups.size(); j++) {
            for (Element e2: groups.get(j)) {
              int ie2 = e2.getId();
              ppm[ie1][ie2] += entry.getValue();
            }
          }
        }
      }
    }
  }

  public double[][] getMatrix() {
    return ppm;
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
