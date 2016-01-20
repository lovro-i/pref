package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.rating.Ratings;
import edu.drexel.cs.db.rank.rating.RatingsSample;
import edu.drexel.cs.db.rank.core.Sample;
import java.util.List;
import java.util.Map.Entry;


public class PairwisePreferenceMatrix {

  private ItemSet itemSet;
  private double[][] ppm;
  
  
  public PairwisePreferenceMatrix(Sample sample) {
    this.itemSet = sample.getItemSet();
    int n = itemSet.size();
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
    this.itemSet = sample.getItems();
    int n = itemSet.size();
    ppm = new double[n][n];
    
    for (Entry<Ratings, Double> entry: sample.entrySet()) {
    
      List<List<Item>> groups = entry.getKey().getGroups();
      for (int i = 0; i < groups.size()-1; i++) {
        for (Item e1: groups.get(i)) {
          int ie1 = e1.getId();
          for (int j = i+1; j < groups.size(); j++) {
            for (Item e2: groups.get(j)) {
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
  
  
  public ItemSet getItemSet() {
    return itemSet;
  }
  
  public double getProbabilityBefore(int i, int j) {
    double sum = ppm[i][j] + ppm[j][i];
    if (sum == 0) return 0.5d;
    return ppm[i][j] / sum;
  }
  
}
