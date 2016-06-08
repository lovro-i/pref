package edu.drexel.cs.db.db4pref.core;

import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.util.*;

/** An ordering (complete or partial) over the set of items */
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

  @Override
  public Ranking clone() {
    return new Ranking(this);
  }

  @Override
  public ItemSet getItemSet() {
    return itemSet;
  }

  public Set<Item> getMissingItems() {
    Set<Item> missing = new HashSet<Item>();
    for (Item e : itemSet) {
      if (!this.contains(e)) {
        missing.add(e);
      }
    }
    return missing;
  }

  /**
   * Returns map of Item to index (position in the ranking
   */
  public Map<Item, Integer> getIndexMap() {
    Map<Item, Integer> map = new HashMap<Item, Integer>();
    for (int i = 0; i < this.length(); i++) {
      Item e = this.get(i);
      map.put(e, i);
    }
    return map;
  }

  /**
   * Shuffles the items in this ranking
   */
  public void randomize() {
    for (int i = 0; i < this.length() - 1; i++) {
      int j = i + MathUtils.RANDOM.nextInt(length() - i);
      swap(i, j);
    }
  }

  /**
   * Adds Item e at the end of the ranking
   */
  public void add(Item e) {
    if (this.contains(e)) {
      throw new IllegalArgumentException("Item " + e + " already in the sample: " + this.toString());
    }
    items.add(e);
  }

  /**
   * Same as add(Item e). Appends Item e at the end of the ranking
   */
  public void append(Item e) {
    this.add(e);
  }

  /**
   * Append ranking r to the end of the ranking
   */
  public void append(Ranking r) {
    for (Item e : r.getItems()) {
      this.add(e);
    }
  }

  public Item set(int index, Item e) {
    return items.set(index, e);
  }

  /**
   * Add Item e at the specified position in the ranking (shifting the ones on the right). If index &gt;= size of the
   * item, add at the end
   */
  public void add(int index, Item e) {
    if (this.contains(e)) {
      throw new IllegalArgumentException(String.format("Item %s already in %s", e, this));
    }
    if (index >= items.size()) {
      items.add(e);
    } else {
      items.add(index, e);
    }
  }

  /**
   * Same as add(int index, Item e). Adds Item e at the specified position in the ranking (shifting the ones on the
   * right). If index &gt;= size of the item, add at the end.
   */
  public void insert(int index, Item e) {
    this.add(index, e);
  }

  /**
   * Return the ranking containing only the items from the collection, in the same order as this ranking
   */
  @Override
  public Ranking toRanking(Collection<Item> items) {
    Ranking r = new Ranking(this.getItemSet());
    for (Item i : this.items) {
      if (items.contains(i)) {
        r.add(i);
      }
    }
    return r;
  }

  @Override
  public Ranking project(Collection<Item> items) {
    return toRanking(items);
  }

  public Ranking top(int k) {
    if (k >= this.length()) {
      return new Ranking(this);
    }
    Ranking top = new Ranking(this.itemSet);
    for (int i = 0; i < k; i++) {
      top.add(this.get(i));
    }
    return top;
  }

  public Ranking bottom(int k) {
    if (k >= this.length()) {
      return new Ranking(this);
    }
    Ranking bottom = new Ranking(this.itemSet);
    for (int i = this.length() - k; i < this.length(); i++) {
      bottom.add(this.get(i));
    }
    return bottom;
  }

  public void remove(int index) {
    items.remove(index);
  }

  @Override
  public boolean remove(Item item) {
    int index = this.indexOf(item);
    if (index == -1) {
      return false;
    }
    remove(index);
    return true;
  }

  /**
   * Add Item e at the random position in the ranking
   */
  public void addAtRandom(Item e) {
    int index = random.nextInt(items.size() + 1);
    this.add(index, e);
  }

  /* List of items in this ranking */
  @Override
  public List<Item> getItems() {
    return items;
  }

  public boolean contains(Item e) {
    return items.contains(e);
  }

  /**
   * Number of items in this ranking.
   */
  public int length() {
    return items.size();
  }

  /**
   * Number of pairs in this ranking (n * (n - 1) / 2).
   */
  @Override
  public int size() {
    return items.size() * (items.size() - 1) / 2;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < items.size(); i++) {
      if (i != 0) {
        sb.append(DELIMITER);
      }
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

  /**
   * Return the item at i-th place in the ranking
   */
  public Item get(int i) {
    return items.get(i);
  }

  /**
   * Returns the index of the given item, -1 if it's not in the ranking
   */
  public int indexOf(Item e) {
    return items.indexOf(e);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    Ranking ranking = (Ranking) o;
    if (this.items.size() != ranking.items.size()) {
      return false;
    }
    for (int i = 0; i < items.size(); i++) {
      if (!this.items.get(i).equals(ranking.items.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 371 + Objects.hashCode(this.items);
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
  public Boolean isPreferred(Item higher, Item lower) {
    Integer ih = null;
    Integer il = null;
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).equals(higher)) {
        ih = i;
        if (il != null) {
          return ih < il;
        }
      } else if (items.get(i).equals(lower)) {
        il = i;
        if (ih != null) {
          return ih < il;
        }
      }
    }
    return null;
  }

  @Override
  public Boolean isPreferred(int higher, int lower) {
    Integer ih = null;
    Integer il = null;
    for (int i = 0; i < items.size(); i++) {
      int id = items.get(i).getId();
      if (id == higher) {
        ih = i;
        if (il != null) {
          return ih < il;
        }
      } else if (id == lower) {
        il = i;
        if (ih != null) {
          return ih < il;
        }
      }
    }
    return null;
  }

  @Override
  public MapPreferenceSet transitiveClosure() {
    MapPreferenceSet tc = new MapPreferenceSet(this.itemSet);
    for (int i = 0; i < length() - 1; i++) {
      Item h = get(i);
      for (int j = i + 1; j < length(); j++) {
        tc.add(h, get(j));
      }
    }
    return tc;
  }

  @Override
  public Set<Item> getHigher(Item item) {
    Set<Item> higher = new HashSet<Item>();
    if (this.contains(item)) {
      for (Item it : this.items) {
        if (item.equals(it)) {
          return higher;
        } else {
          higher.add(it);
        }
      }
    }
    return higher;
  }

  @Override
  public Set<Item> getLower(Item item) {
    Set<Item> lower = new HashSet<Item>();
    if (this.contains(item)) {
      for (int i = length() - 1; i > 0; i--) {
        Item it = this.items.get(i);
        if (item.equals(it)) {
          return lower;
        } else {
          lower.add(it);
        }
      }
    }
    return lower;
  }

  public boolean isConsistent(PreferenceSet v) {
    for (int i = 0; i < length() - 1; i++) {
      Item i1 = this.get(i);
      for (int j = i + 1; j < length(); j++) {
        Item i2 = this.get(j);
        Boolean p = v.isPreferred(i1, i2);
        if (p != null && p == false) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean contains(Item higher, Item lower) {
    Boolean h = isPreferred(higher, lower);
    if (h == null) {
      return false;
    }
    return h;
  }

  @Override
  public boolean contains(Preference pref) {
    return contains(pref.higher, pref.lower);
  }

  @Override
  public boolean contains(int higherId, int lowerId) {
    Boolean h = isPreferred(higherId, lowerId);
    if (h == null) {
      return false;
    }
    return h;
  }

  @Override
  public Set<Preference> getPreferences() {
    Set<Preference> prefs = new HashSet<Preference>();
    for (int i = 0; i < length() - 1; i++) {
      Item h = get(i);
      for (int j = i + 1; j < length(); j++) {
        prefs.add(new Preference(h, get(j)));
      }
    }
    return prefs;
  }

  @Override
  public Set<Ranking> getRankings() {
    Set<Ranking> r = new HashSet<Ranking>();
    r.add(this);
    return r;
  }

}
