package edu.drexel.cs.db.rank.kemeny;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Histogram;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** Quickly find a good complete candidate to start kemenization from */
public class KemenyCandidate {

  /** Quickly find a good complete candidate to start kemenization from */
  public static Ranking find(Sample<Ranking> sample) {
    int n = sample.getItemSet().size();
    Ranking longest = null;
    Histogram<Ranking> rankHist = new Histogram();
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i).p;
      if (longest == null || r.length() > longest.length()) longest = r;
      if (r.length() == n) rankHist.add(r, sample.getWeight(i));
    }
    
    if (rankHist.isEmpty()) return complete(longest);
    else return rankHist.getMostFrequent();
  }
  
  
  public static Ranking complete(PreferenceSet p) {
    Ranking complete = toIncompleteRanking(p);
    for (Item e: p.getItemSet()) {
      if (!complete.contains(e)) complete.add(e);
    }
    return complete;
  }
  
  public static Ranking toIncompleteRanking(PreferenceSet p) {
    HashMap<Integer, HashSet<Item>> numToItem = new HashMap<>();
    HashSet<Item> availableItems = new HashSet<>();
    availableItems.addAll(p.getItems());
    for (Item e : availableItems) {
      int numChildren = 0;
      int numAncestors = 0;
      Set<Item> lo = p.getLower(e);
      if (lo != null) numChildren = lo.size();

      Set<Item> hi = p.getHigher(e);
      if (hi != null) numAncestors = hi.size();

      int preferenceIdx = numChildren - numAncestors;
      if (numToItem.containsKey(preferenceIdx)) {
        numToItem.get(preferenceIdx).add(e);
      }
      else {
        HashSet<Item> tmpSet = new HashSet<>();
        tmpSet.add(e);
        numToItem.put(preferenceIdx, tmpSet);
      }
    }

    ItemSet items = p.getItemSet();
    Ranking r = new Ranking(items);
    for (int i = items.size(); i >= -items.size(); i--) {
      if (numToItem.containsKey(i)) {
        for (Item e : numToItem.get(i)) {
          r.add(e);
        }
      }
    }
    return r;
  }
  
}
