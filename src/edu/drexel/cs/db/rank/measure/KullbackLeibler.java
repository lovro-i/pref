package edu.drexel.cs.db.rank.measure;

import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.ppm.PairwisePreferenceMatrix;

/** Calculates Kullback-Leibler divergence between two PPMs */
public class KullbackLeibler {

  public static double divergence(Sample real, Sample model) {
    return divergence(new PairwisePreferenceMatrix(real), new PairwisePreferenceMatrix(model));
  }
  
  public static double divergence(PairwisePreferenceMatrix real, PairwisePreferenceMatrix model) {
    if (!real.getElements().equals(model.getElements())) return Double.NaN;
    
    int n = real.getElements().size();
    double sum = 0;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i != j) {
          double p = real.getProbabilityBefore(i, j);
          if (p != 0) {
            double q = model.getProbabilityBefore(i, j);
            if (q != 0) sum += p * Math.log(p / q);
            else return Double.POSITIVE_INFINITY;
          }
        }
      }      
    }
    
    return sum / (n * (n - 1));
  }
  
}
