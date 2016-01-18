package edu.drexel.cs.db.rank.ppm;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;

/** Simple distance between two samples / PPMs by averaging the difference on each field */
public class PPMDistance {

  public static double distance(Sample sample1, Sample sample2) {    
    return distance(new PairwisePreferenceMatrix(sample1), new PairwisePreferenceMatrix(sample2));
  }
  
  public static double distance(PairwisePreferenceMatrix ppm1, PairwisePreferenceMatrix ppm2) {    
    if (!ppm1.getElements().equals(ppm2.getElements())) return Double.NaN;
    
    int n = ppm1.getElements().size();
    double sum = 0;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i != j) {
          double p1 = ppm1.getProbabilityBefore(i, j);
          double p2 = ppm2.getProbabilityBefore(i, j);
          double d = p1 - p2;
          sum += Math.abs(d); // * d;
        }
      }      
    }
    
    return sum / (n * (n -1));
  }
  
  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(20);
    Sample s1 = MallowsUtils.sample(elements.getReferenceRanking(), 0.5, 5000);
    Sample s2 = MallowsUtils.sample(elements.getReferenceRanking(), 0.4, 5000);
    
    Logger.info("Distance: %.4f", distance(s1, s2));
  }
  
}
