package com.rankst.incomplete;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;


public class IncompleteUtils {

  public static double getMissingRate(Sample sample) {
    ElementSet elements = sample.getElements();
    long count = 0;
    for (Ranking r: sample) {
      count += r.size();
    }
    long total = sample.size() * elements.size();
    return 1d * (total - count) / total;
  }  
  
}
