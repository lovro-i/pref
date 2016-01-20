package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Sample;


public class IncompleteUtils {

  public static double getMissingRate(Sample sample) {
    ItemSet items = sample.getItemSet();    
    double count = 0;
    double total = 0;    
    for (Sample.RW rw: sample.enumerate()) {
      count += rw.w * rw.r.size();
      total += rw.w * items.size();
    }
    return 1d * (total - count) / total;
  }  
  
}
