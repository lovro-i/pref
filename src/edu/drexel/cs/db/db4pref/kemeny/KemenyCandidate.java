package edu.drexel.cs.db.db4pref.kemeny;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.util.Histogram;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** Quickly find a good complete candidate to start kemenization from */
public class KemenyCandidate {

  /** Quickly find a good complete candidate to start kemenization from */
  public static Ranking find(Sample<? extends PreferenceSet> sample) {
    int n = sample.getItemSet().size();
    Ranking longest = null;
    Histogram<Ranking> rankHist = new Histogram();
    for (int i = 0; i < sample.size(); i++) {
      PreferenceSet pref = sample.get(i).p;
      if (pref instanceof Ranking) {
        Ranking r = (Ranking) pref;      
        if (longest == null || r.length() > longest.length()) longest = r;
        if (r.length() == n) rankHist.add(r, sample.getWeight(i));
      }
    }
    
    if (!rankHist.isEmpty()) return rankHist.getMostFrequent();
    else if (longest != null) return complete(longest);
    else return sample.getItemSet().getRandomRanking();
  }
  
  
  public static Ranking complete(PreferenceSet p) {
    Ranking complete = toRanking(p);
    for (Item e: p.getItemSet()) {
      if (!complete.contains(e)) complete.add(e);
    }
    return complete;
  }
  
  /** Converts a PreferenceSet to an (incomplete) ranking to use as a candidate for kemenization
   * @author Haoyue
   */
  public static Ranking toRanking(PreferenceSet p) {
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
