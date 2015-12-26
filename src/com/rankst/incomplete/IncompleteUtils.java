package com.rankst.incomplete;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;


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
