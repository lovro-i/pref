package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.*;


public class Ranking implements Comparable, PreferenceSet {

  private static final Random random = new Random();
  private static final String DELIMITER = "-";
  private static final String DELIMITERS = "-, >;\t";
  
  protected ItemSet itemSet;
  protected List<Item> items = new ArrayList<Item>();
  
  public Ranking(ItemSet itemSet) {
    this.itemSet = itemSet;
  }
  
  public Ranking(Ranking ranking) {
    this.itemSet = ranking.itemSet;
    this.items.addAll(ranking.items);
  }

  public ItemSet getItemSet() {
    return itemSet;
  }
  
  public Set<Item> getMissingItems() {
    Set<Item> missing = new HashSet<Item>();
    for (Item e: itemSet) {
      if (!this.contains(e)) missing.add(e);
    }
    return missing;
  }
  
  /** Returns map of Item to index (position in the ranking */
  public Map<Item, Integer> getIndexMap() {
    Map<Item, Integer> map = new HashMap<Item, Integer>();
    for (int i = 0; i < this.size(); i++) {
      Item e = this.get(i);
      map.put(e, i);
    }    
    return map;
  }
    
  /** Shuffles the items in this ranking */
  public void randomize() {
    for (int i = 0; i < this.size() - 1; i++) {
      int j = i + MathUtils.RANDOM.nextInt(size() - i);
      swap(i, j);
    }
  }
  
  /** Add Item e at the end of the ranking */
  public void add(Item e) {
    if (this.contains(e)) throw new IllegalArgumentException("Item " + e + " already in the sample: " + this.toString());
    items.add(e);
  }
  
  /** Append ranking r to the end of the ranking */
  public void add(Ranking r) {
    for (Item e: r.getItems()) this.add(e);
  }
  
  public Item set(int index, Item e) {
    return items.set(index, e);
  }
  
  /** Add Item e at the specified position in the ranking (shifting the ones on the right). If index >= size of the item, add at the end */
  public Ranking addAt(int index, Item e) {
    if (this.contains(e)) throw new IllegalArgumentException(String.format("Item %s already in %s", e, this));
    if (index >= items.size()) items.add(e);
    else items.add(index, e);
    return this;
  }

  /** Return the ranking containing only the items from the collection, in the same order as this ranking */
  public Ranking project(Collection<Item> items) {
    Ranking r = new Ranking(this.getItemSet());
    for (Item i: this.items) {
      if (items.contains(i)) r.add(i);
    }
    return r;
  }
  
  public Ranking remove(int index) {
    items.remove(index);
    return this;
  }
  
  
  /** Add Item e at the random position in the ranking */
  public void addAtRandom(Item e) {
    int index = random.nextInt(items.size()+1);
    this.addAt(index, e);
  }
  
  
  public List<Item> getItems() {
    return items;
  }
  
  public boolean contains(Item e) {
    return items.contains(e);
  }
  
  /** Number of items in this ranking. */
  public int size() {
    return items.size();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<items.size(); i++) {
      if (i != 0) sb.append(DELIMITER);
      sb.append(items.get(i));
    }
    return sb.toString();
  }

  public void swap(int i1, int i2) {
    Item e1 = items.get(i1);
    Item e2 = items.get(i2);
    items.set(i1, e2);
    items.set(i2, e1);
  }

  /** Return the item at i-th place in the ranking */
  public Item get(int i) {
    return items.get(i);
  }
  
  /** Returns the index of the given item, -1 if it's not in the ranking */
  public int indexOf(Item e) {
    return items.indexOf(e);
  }
  
  @Override
  public boolean equals(Object o) {
    Ranking ranking = (Ranking) o;
    if (this.items.size() != ranking.items.size()) return false;
    for (int i=0; i<items.size(); i++) {
      if (!this.items.get(i).equals(ranking.items.get(i))) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.items);
    return hash;
  }
  
  public static Ranking fromStringById(ItemSet itemSet, String s) {
    Ranking ranking = new Ranking(itemSet);
    StringTokenizer st = new StringTokenizer(s, DELIMITERS);
    while (st.hasMoreTokens()) {
      String t = st.nextToken();
      int id = Integer.parseInt(t);
      Item e = itemSet.getItemById(id);
      ranking.add(e);
    }
    return ranking;
  }    
  
  public static Ranking fromStringByTag(ItemSet itemSet, String s) {
    Ranking ranking = new Ranking(itemSet);
    StringTokenizer st = new StringTokenizer(s, DELIMITERS);
    while (st.hasMoreTokens()) {
      String t = st.nextToken();
      Item e = itemSet.getItemByTag(t);
      ranking.add(e);
    }
    return ranking;
  }

  @Override
  public int compareTo(Object o) {
    return this.toString().compareTo(o.toString());
  }

  @Override
  public Boolean isHigher(Item higher, Item lower) {
    Integer ih = null;
    Integer il = null;
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).equals(higher)) {
        ih = i;
        if (il != null) return ih < il;
      }
      else if (items.get(i).equals(lower)) {
        il = i;
        if (ih != null) return ih < il;
      }
    }
    return null;
  }
  
  @Override
  public Boolean isHigher(int higher, int lower) {
    Integer ih = null;
    Integer il = null;
    for (int i = 0; i < items.size(); i++) {
      int id = items.get(i).getId();
      if (id == higher) {
        ih = i;
        if (il != null) return ih < il;
      }
      else if (id == lower) {
        il = i;
        if (ih != null) return ih < il;
      }
    }
    return null;
  }

  @Override
  public DensePreferenceSet transitiveClosure() {
    DensePreferenceSet tc = new DensePreferenceSet(this.itemSet);
    for (int i = 0; i < size()-1; i++) {
      for (int j = i+1; j < size(); j++) {
        tc.add(get(i), get(j));
      }
    }
    return tc;
  }

  @Override
  public Set<Item> getHigher(Item item) {
    Set<Item> higher = new HashSet<Item>();
    if (this.contains(item)) {
      for (Item it: this.items) {
        if (item.equals(it)) return higher;
        else higher.add(it);
      }
    }
    return higher;
  }

  @Override
  public Set<Item> getLower(Item item) {
    Set<Item> lower = new HashSet<Item>();
    if (this.contains(item)) {
      for (int i = size()-1; i > 0; i--) {
        Item it = this.items.get(i);
        if (item.equals(it)) return lower;
        else lower.add(it);
      }
    }
    return lower;
  }
  
  public boolean isConsistent(PreferenceSet v) {
    for (int i = 0; i < size()-1; i++) {
      for (int j = i+1; j < size(); j++) {
         Boolean p = v.isHigher(i, j);
         if (p != null && p == false) return false;
      }
    }
    return true;
  }
  
}
 