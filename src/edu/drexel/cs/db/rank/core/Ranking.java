package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.*;


public class Ranking implements Comparable {

  private static final Random random = new Random();
  private static final String DELIMITER = "-";
  private static final String DELIMITERS = "-, >;\t";
  
  private ItemSet itemSet;
  private List<Item> items = new ArrayList<Item>();
  
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
    if (index >= items.size()) items.add(e);
    else items.add(index, e);
    return this;
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
  
  
}
 