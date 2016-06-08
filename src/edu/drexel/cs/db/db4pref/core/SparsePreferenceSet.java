package edu.drexel.cs.db.db4pref.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** MutablePreferenceSet which stores preference pairs in a list of Preferences */
public class SparsePreferenceSet extends AbstractPreferenceSet implements Set<Preference> {

  private final ItemSet items;
  private final Set<Preference> preferences = new HashSet<Preference>();

  public SparsePreferenceSet(ItemSet itemSet) {
    this.items = itemSet;
  }
  
  public SparsePreferenceSet(PreferenceSet prefs) {
    this(prefs.getItemSet());
    for (Preference pref: prefs.getPreferences()) this.add(pref.higher, pref.lower);
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
  
  public Boolean isPreferred(Item higher, Item lower) {
    for (Preference p: this) {
      if (p.higher.equals(higher) && p.lower.equals(lower)) return true;
      if (p.higher.equals(lower) && p.lower.equals(higher)) return false;
    }
    return null;
  }
  
  @Override
  public Boolean isPreferred(int hid, int lid) {
    return isPreferred(items.get(hid), items.get(lid));
  }
  
  @Override
  public boolean addById(int higherId, int lowerId) {
    return this.add(items.get(higherId), items.get(lowerId));
  }
  
    @Override
  public boolean addByTag(Object higherTag, Object lowerTag) {
    return add(items.getItemByTag(higherTag), items.getItemByTag(lowerTag));
  }
  
  @Override
  public boolean add(Item higher, Item lower) {
    MapPreferenceSet mps = new MapPreferenceSet(this);
    if (!mps.checkAcyclic(higher, lower)) {
      throw new IllegalStateException(String.format("Cannot add (%s, %s) pair, graph would be cyclic", higher, lower));
    }
    
    Preference pref = new Preference(higher, lower);
    return this.add(pref);
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
  public MapPreferenceSet transitiveClosure() {
    MapPreferenceSet tc = new MapPreferenceSet(this);
    tc.transitiveClose();
    return tc;
  }
  
  @Override
  public void transitiveClose() {
    throw new UnsupportedOperationException("Not supported yet.");
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
    Boolean result = this.isPreferred(item1, item2);
    if (result != null) {
      if (result) this.remove(new Preference(item1, item2));
      else this.remove(new Preference(item2, item1));
    }
    return result;
  }

  @Override
  public Boolean remove(int itemId1, int itemId2) {
    return this.remove(items.get(itemId1), items.get(itemId2));
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
  public Ranking toRanking(Collection<Item> items) {
    return this.transitiveClosure().toRanking(items);
  }
    
    
  @Deprecated
  private Ranking toRankingDirect(Collection<Item> items) {  
    Map<Item, Integer> itemCount = new HashMap<Item, Integer>();
    for (Item item: items) itemCount.put(item, 0);
    List<Item> itemList = new ArrayList<Item>(items);
    
    for (int i = 0; i < itemList.size() - 1; i++) {
      Item it1 = itemList.get(i);
      for (int j = i+1; j < itemList.size(); j++) {
        Item it2 = itemList.get(j);
        Boolean b = this.isPreferred(it1, it2);
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
  public SparsePreferenceSet project(Collection<Item> items) {
    SparsePreferenceSet projection = new SparsePreferenceSet(this);
    for (Preference pref: this.getPreferences()) {
      if (items.contains(pref.higher) && items.contains(pref.lower)) projection.add(pref);
    }
    return projection;
  }

  @Override
  public boolean contains(Item item) {
    for (Preference pref: this) {
      if (pref.contains(item)) return true;
    }
    return false;
  }

  @Override
  public Set<Preference> getPreferences() {
    return this;
  }

  @Override
  public boolean remove(Item item) {
    boolean removed = false;
    Iterator<Preference> it = this.iterator();
    while (it.hasNext()) {
      Preference pref = it.next();
      if (pref.contains(item)) {
        removed = true;
        it.remove();
      }
    }
    return removed;
  }

  @Override
  public boolean remove(Preference pref) {
    return preferences.remove(pref);
  }

  @Override
  public boolean contains(Preference pref) {
    return preferences.contains(pref);
  }

  @Override
  public Set<Item> getItems() {
    Set<Item> items = new HashSet<Item>();
    for (Preference p: this) {
      items.add(p.higher);
      items.add(p.lower);
    }
    return items;
  }

  @Override
  public int size() {
    return preferences.size();
  }

  @Override
  public boolean isEmpty() {
    return preferences.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return preferences.isEmpty();
  }

  @Override
  public Iterator<Preference> iterator() {
    return preferences.iterator();
  }

  @Override
  public Object[] toArray() {
    return preferences.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return preferences.toArray(a);
  }

  @Override
  public boolean add(Preference e) {
    return preferences.add(e);
  }

  @Override
  public boolean remove(Object o) {
    return preferences.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return preferences.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends Preference> c) {
    return preferences.addAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return preferences.retainAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return preferences.removeAll(c);
  }

  @Override
  public void clear() {
    preferences.clear();
  }

  
}
