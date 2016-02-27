package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SparsePreferenceSet extends HashSet<Preference> implements MutablePreferenceSet {

  private final ItemSet items;

  public SparsePreferenceSet(ItemSet itemSet) {
    this.items = itemSet;
  }

  @Override
  public SparsePreferenceSet clone() {
    SparsePreferenceSet sps = new SparsePreferenceSet(items);
    sps.addAll(this);
    return sps;
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
  
  @Override
  public boolean add(int higherId, int lowerId) {
    return this.add(items.get(higherId), items.get(lowerId));
  }
  
  @Override
  public boolean add(Item higher, Item lower) {
    if (!items.contains(higher)) throw new IllegalArgumentException("Item " + higher + " not in the set");
    if (!items.contains(lower)) throw new IllegalArgumentException("Item " + lower + " not in the set");
    
    Preference pref = new Preference(higher, lower);
    if (this.contains(pref)) return false;
    this.remove(lower, higher);
    this.add(pref);
    return true;
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



  @Override
  public Boolean remove(Item item1, Item item2) {
    Boolean result = this.isHigher(item1, item2);
    if (result != null) {
      if (result) this.remove(new Preference(item1, item2));
      else this.remove(new Preference(item2, item1));
    }
    return result;
  }

  @Override
  public Boolean remove(int idemId1, int itemId2) {
    return this.remove(items.get(idemId1), items.get(itemId2));
  }

  @Override
  public boolean contains(Item higher, Item lower) {
    return this.contains(new Preference(higher, lower));
  }

  @Override
  public boolean contains(int higherId, int lowerId) {
    return this.contains(items.get(higherId), items.get(lowerId));
  }
  
  /** Create ranking from the items in the collection, if possible */
  @Override
  public Ranking project(Collection<Item> items) {
    Map<Item, Integer> itemCount = new HashMap<Item, Integer>();
    for (Item item: items) itemCount.put(item, 0);
    List<Item> itemList = new ArrayList<Item>(items);
    
    for (int i = 0; i < itemList.size() - 1; i++) {
      Item it1 = itemList.get(i);
      for (int j = i+1; j < itemList.size(); j++) {
        Item it2 = itemList.get(j);
        Boolean b = this.isHigher(it1, it2);
        if (b == null) return null;
        if (b) {
          int c = itemCount.get(it2);
          itemCount.put(it2, c+1);
        }
        else {
          int c = itemCount.get(it1);
          itemCount.put(it1, c+1);
        }
      }      
    }
    
    Map<Integer, Item> reverse = new HashMap<Integer, Item>();
    for (Item it: itemCount.keySet()) reverse.put(itemCount.get(it), it);
    
    Ranking top = new Ranking(getItemSet());
    for (int i = 0; i < itemList.size(); i++) {
      top.add(reverse.get(i));
    }    
    return top;
  }

  @Override
  public boolean contains(Item item) {
    for (Preference pref: this) {
      if (pref.contains(item)) return true;
    }
    return false;
  }
  
}
