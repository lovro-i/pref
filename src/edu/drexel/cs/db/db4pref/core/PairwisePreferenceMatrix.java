package edu.drexel.cs.db.db4pref.core;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.Sample.PW;


public class PairwisePreferenceMatrix {

  private ItemSet itemSet;
  private double[][] ppm;
  
  
  public PairwisePreferenceMatrix(Sample<? extends PreferenceSet> sample) {
    this.itemSet = sample.getItemSet();
    int n = itemSet.size();
    ppm = new double[n][n];
    
    for (PW pw: sample) {
      PreferenceSet tc = pw.p.transitiveClosure();
      for (Preference pref: tc.getPreferences()) {
        ppm[pref.higher.id][pref.lower.id] += pw.w;
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
