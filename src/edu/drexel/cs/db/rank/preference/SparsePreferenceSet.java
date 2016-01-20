package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.preference.Preference;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.HashSet;


public class SparsePreferenceSet extends HashSet<Preference> implements PreferenceSet {

  private final ItemSet itemSet;

  public SparsePreferenceSet(ItemSet itemSet) {
    this.itemSet = itemSet;
  }

  public void add(Item higher, Item lower) {
    if (!itemSet.contains(higher)) throw new IllegalArgumentException("Item " + higher + " not in the set");
    if (!itemSet.contains(lower)) throw new IllegalArgumentException("Item " + lower + " not in the set");
    
    Preference pref = new Preference(higher, lower);
    this.add(pref);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Preference pref: this) {
      sb.append(pref).append("\n");
    }
    sb.append("===== ").append(this.size()).append(" preference pairs =====");
    return sb.toString();
  }
  
  @Override
  public Sample toSample() {
    Sample sample = new Sample(itemSet);
    for (Preference pref: this) {
      Ranking r = new Ranking(itemSet);
      r.add(pref.higher);
      r.add(pref.lower);
      sample.add(r);
    }
    return sample;
  }
  
}
