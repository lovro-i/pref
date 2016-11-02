package edu.drexel.cs.db.db4pref.distance;

import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.PairwisePreferenceMatrix;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;

/** Calculates Kullback-Leibler divergence between two PPMs */
public class KL {

  public static double divergence(Sample<? extends PreferenceSet> real, Sample<? extends PreferenceSet> model) {
    return divergence(new PairwisePreferenceMatrix(real), new PairwisePreferenceMatrix(model));
  }
  
  public static double divergence(PairwisePreferenceMatrix real, PairwisePreferenceMatrix model) {
    if (!real.getItemSet().equals(model.getItemSet())) return Double.NaN;
    
    int n = real.getItemSet().size();
    double sum = 0;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i != j) {
          double p = real.getProbabilityBefore(i, j) + 0.0000001;
          if (p != 0) {
            double q = model.getProbabilityBefore(i, j) + 0.0000001;
            if (q != 0) sum += p * Math.log(p / q);
            else return Double.POSITIVE_INFINITY;
          }
        }
      }      
    }
    
    return sum / (n * (n - 1));
  }
  
  public static double divergenceIgnoreZero(Sample<? extends PreferenceSet> real, Sample<? extends PreferenceSet> model) {
    return divergenceIgnoreZero(new PairwisePreferenceMatrix(real), new PairwisePreferenceMatrix(model));
  }
  
  public static double divergenceIgnoreZero(PairwisePreferenceMatrix real, PairwisePreferenceMatrix model) {
    if (!real.getItemSet().equals(model.getItemSet())) return Double.NaN;
    
    int n = real.getItemSet().size();
    double sum = 0;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i != j) {
          double p = real.getProbabilityBefore(i, j);
          if (p != 0) {
            double q = model.getProbabilityBefore(i, j);
            if (q != 0) sum += p * Math.log(p / q);
          }
        }
      }      
    }
    
    return sum / (n * (n - 1));
  }
}
