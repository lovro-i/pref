package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import java.util.HashSet;
import java.util.Set;


public class SparsePreferenceSet extends HashSet<Preference> implements PreferenceSet {

  private final ItemSet items;

  public SparsePreferenceSet(ItemSet itemSet) {
    this.items = itemSet;
  }

  @Override
  public ItemSet getItemSet() {
    return items;
  }
  
  public Boolean isHigher(Item higher, Item lower) {
    if (this.contains(new Preference(higher, lower))) return true;
    if (this.contains(new Preference(lower, higher))) return false;
    return null;
  }
  
    @Override
  public Boolean isHigher(int hid, int lid) {
    return isHigher(items.get(hid), items.get(lid));
  }
  
  public void add(Item higher, Item lower) {
    if (!items.contains(higher)) throw new IllegalArgumentException("Item " + higher + " not in the set");
    if (!items.contains(lower)) throw new IllegalArgumentException("Item " + lower + " not in the set");
    
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
  
 
  public DensePreferenceSet toDense() {
    DensePreferenceSet dense = new DensePreferenceSet(items);
    for (Preference pref: this) {
      dense.add(pref.higher, pref.lower);
    }
    return dense;
  }
  
  @Override
  public DensePreferenceSet transitiveClosure() {
    return this.toDense().transitiveClosure();
  }

  @Override
  public Set<Item> getHigher(Item i) {
    Set<Item> set = new HashSet<Item>();
    for (Preference pref: this) {
      if (pref.lower.equals(i)) set.add(pref.higher);
    }
    return set;
  }

  @Override
  public Set<Item> getLower(Item i) {
    Set<Item> set = new HashSet<Item>();
    for (Preference pref: this) {
      if (pref.higher.equals(i)) set.add(pref.lower);
    }
    return set;
  }
  
}
