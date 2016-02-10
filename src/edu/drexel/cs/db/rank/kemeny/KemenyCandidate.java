package edu.drexel.cs.db.rank.kemeny;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.util.Histogram;

/** Quickly find a good complete candidate to start kemenization from */
public class KemenyCandidate {

  /** Quickly find a good complete candidate to start kemenization from */
  public static Ranking find(Sample sample) {
    int n = sample.getItemSet().size();
    Ranking longest = null;
    Histogram<Ranking> rankHist = new Histogram();
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i).r;
      if (longest == null || r.size() > longest.size()) longest = r;
      if (r.size() == n) rankHist.add(r, sample.getWeight(i));
    }
    
    if (rankHist.isEmpty()) return complete(longest);
    else return rankHist.getMostFrequent();
  }
  
  
  public static Ranking complete(Ranking r) {
    Ranking complete = new Ranking(r);
    for (Item e: r.getItemSet()) {
      if (!complete.contains(e)) complete.add(e);
    }
    return complete;
  }
  
}
