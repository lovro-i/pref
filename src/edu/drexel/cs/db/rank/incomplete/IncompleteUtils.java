package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;


public class IncompleteUtils {

  public static double getMissingRate(Sample sample) {
    ElementSet elements = sample.getElements();    
    double count = 0;
    double total = 0;    
    for (Sample.RW rw: sample.enumerate()) {
      count += rw.w * rw.r.size();
      total += rw.w * elements.size();
    }
    return 1d * (total - count) / total;
  }  
  
}
