package com.rankst.kemeny;

import com.rankst.entity.Element;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.histogram.Histogram;
import java.util.List;

/** Quickly find a good complete candidate to start kemenization from */
public class KemenyCandidate {

  /** Quickly find a good complete candidate to start kemenization from */
  public static Ranking find(Sample sample) {
    int n = sample.getElements().size();
    Ranking longest = null;
    Histogram<Ranking> rankHist = new Histogram();
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i);
      if (longest == null || r.size() > longest.size()) longest = r;
      if (r.size() == n) rankHist.add(r, sample.getWeight(i));
    }
    
    if (rankHist.isEmpty()) return complete(longest);
    else return rankHist.getMostFrequent();
  }
  
  
  public static Ranking complete(Ranking r) {
    Ranking complete = new Ranking(r);
    List<Element> elems = r.getElementSet().getElements();
    for (Element e: elems) {
      if (!complete.contains(e)) complete.add(e);
    }
    return complete;
  }
  
}
