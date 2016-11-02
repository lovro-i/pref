package edu.drexel.cs.db.db4pref.core;

import java.util.HashSet;
import java.util.Set;

/** Abstract implementation of MutablePreferenceSet, so that you can extend it without implementing some things from the scratch. */
public abstract class AbstractPreferenceSet implements MutablePreferenceSet {

  
  public abstract AbstractPreferenceSet clone();

  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PreferenceSet)) return false;
    PreferenceSet pref = (PreferenceSet) o;
    return pref.getPreferences().equals(this.getPreferences());
  }
  
  @Override
  public boolean isEmpty() {
    return size() == 0;
  }
  
  @Override
  public boolean add(Preference p) {
    return add(p.higher, p.lower);
  }
    
    /** @return Set of rankings consistent with this PreferenceSet */
  public Set<Ranking> getRankings() {
    ItemSet items = this.getItemSet();
    Set<Ranking> rankingsIn = new HashSet<Ranking>();
    Set<Ranking> rankingsOut = new HashSet<Ranking>();
    Ranking empty = new Ranking(items);
    rankingsIn.add(empty);
    
    for (Item item: items) {
      if (!this.contains(item)) continue;
      
      rankingsIn.addAll(rankingsOut);
      rankingsOut.clear();
      
      for (Ranking r: rankingsIn) {
        int low = 0;
        int high = r.length();

        Set<Item> higher = this.getHigher(item);
        Set<Item> lower = this.getLower(item);
        for (int j = 0; j < r.length(); j++) {
          Item it = r.get(j);
          if (higher.contains(it)) low = j + 1;
          if (lower.contains(it) && j < high) high = j;
        }

        if (low == high) {
          r.add(low, item);
          rankingsOut.add(r);
        }
        else {
          for (int i = low; i <= high; i++) {
            Ranking r1 = new Ranking(r);
            r1.add(i, item);
            rankingsOut.add(r1);
          }
        }
      }
      
      rankingsIn.clear();
    }
    return rankingsOut;
  }
  
}
