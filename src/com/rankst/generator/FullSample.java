package com.rankst.generator;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;

/** Sample containing all rankings of length n */
public class FullSample extends Sample {

  public FullSample(ElementSet elements) {
    super(elements);
    Ranking r = elements.getReferenceRanking();
    permute(r, 0);
  }
  
  public FullSample(Ranking reference) {
    super(reference.getElementSet());
    permute(reference, 0);
  }
  
  private void permute(Ranking r, int k) {
    for (int i = k; i < r.size(); i++) {
      java.util.Collections.swap(r.getElements(), i, k);
      this.permute(r, k + 1);
      java.util.Collections.swap(r.getElements(), k, i);
    }
    if (k == r.size() - 1) {
      Ranking a = new Ranking(r);
      this.add(a);
    }
  }

  public static void main(String[] args) {
    int n = 5;
    ElementSet elements = new ElementSet(n);
    Ranking reference = elements.getReferenceRanking();
    Sample sample = new FullSample(reference);
    System.out.println(sample);
//    System.out.println(sample.size());
//    
//    
//    Histogram<Integer> h = new Histogram<Integer>();
//    RankingDistance dist = new KendallTauRankingDistance();
//    for (Ranking r: sample) {
//      int d = (int) dist.distance(reference, r);
//      h.add(d);
//      System.out.println(r+": " + d);
//    }
//    System.out.println(n);
//    new HistogramReport(h).out();
  }
  
}
