package edu.drexel.cs.db.db4pref.core;

import cern.colt.Arrays;
import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DensePreferenceSet extends AbstractPreferenceSet {

  private final ItemSet items;
  final boolean[][] higher;

  public DensePreferenceSet(ItemSet items) {
    this.items = items;
    higher = new boolean[items.size()][items.size()];
  }

  private DensePreferenceSet(ItemSet items, boolean[][] a) {
    this.items = items;
    this.higher = a;
  }
  
  public DensePreferenceSet(PreferenceSet prefs) {
    this(prefs.getItemSet());
    for (Preference pref: prefs.getPreferences()) this.add(pref.higher, pref.lower);
  }

  @Override
  public ItemSet getItemSet() {
    return items;
  }

  public boolean[][] getMatrix() {
    return higher;
  }

  @Override
  public DensePreferenceSet clone() {
    DensePreferenceSet dps = new DensePreferenceSet(items);
    for (int i = 0; i < higher.length; i++) {
      for (int j = 0; j < higher.length; j++) {
        dps.higher[i][j] = higher[i][j];
      }
    }
    return dps;
  }

  /**
   * When adding edge(u,v), run BFS from v to check if new edge brings any
   * circle.
   *
   * @return true if it is possible to add (higher, lower) pair
   */
  public boolean checkAcyclic(int higher, int lower) {
    // starting from lower item to see if lower item's spring is higher item's ancestor.
    LinkedList<Integer> openList = new LinkedList<>();
    openList.add(lower);
    while (!openList.isEmpty()) {
      int currentItem = openList.poll();
      for (int i = 0; i < items.size(); i++) {
        if (this.higher[currentItem][i] == true) {
          openList.add(i);
          if (i == higher) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean addById(int higher, int lower) {
    if (!checkAcyclic(higher, lower)) {
      throw new IllegalStateException(String.format("Cannot add (%s, %s) pair, graph would be cyclic", higher, lower));
    }
    
    if (this.higher[higher][lower]) return false;
    
    this.higher[higher][lower] = true;
    this.higher[lower][higher] = false;
    return true;
  }

  
  @Override
  public boolean addByTag(Object higherTag, Object lowerTag) {
    return add(items.getItemByTag(higherTag), items.getItemByTag(lowerTag));
  }
  
  @Override
  public boolean add(Item higher, Item lower) {
    int hid = higher.getId();
    int lid = lower.getId();
    return addById(hid, lid);
  }

  @Override
  public Boolean isPreferred(Item preferred, Item over) {
    int hid = preferred.getId();
    int lid = over.getId();
    if (this.higher[hid][lid]) {
      return true;
    }
    if (this.higher[lid][hid]) {
      return false;
    }
    return null;
  }

  @Override
  public Boolean isPreferred(int hid, int lid) {
    if (this.higher[hid][lid]) {
      return true;
    }
    if (this.higher[lid][hid]) {
      return false;
    }
    return null;
  }

  @Override
  public DensePreferenceSet transitiveClosure() {
    boolean[][] a = higher;
    boolean[][] b = higher;
    boolean[][] c;
    boolean eq;
    do {
      c = multiplyOr(a, b);
      eq = equal(c, b);
      b = c;
    } while (!eq);

    return new DensePreferenceSet(items, c);
  }
  
  @Override
  public void transitiveClose() {
    boolean[][] a = higher;
    boolean[][] b = higher;
    boolean[][] c;
    boolean eq;
    do {
      c = multiplyOr(a, b);
      eq = equal(c, b);
      b = c;
    } while (!eq);

    for (int i = 0; i < higher.length; i++) {
      for (int j = 0; j < higher.length; j++) {
        this.higher[i][j] = c[i][j];
      }
    }
  }

  private boolean[][] multiplyOr(boolean[][] a, boolean[][] b) {
    int n = a.length;
    boolean[][] c = new boolean[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        c[i][j] = false;
        for (int k = 0; k < n; k++) {
          if (a[i][k] && b[k][j]) {
            c[i][j] = true;
            break;
          }
        }
        c[i][j] = c[i][j] || b[i][j];
      }
    }
    return c;
  }

  private boolean equal(boolean[][] a, boolean[][] b) {
    int n = a.length;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (a[i][j] != b[i][j]) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean[][] clone(boolean[][] a) {
    int n = a.length;
    boolean[][] b = new boolean[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        b[i][j] = a[i][j];
      }
    }
    return b;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < higher.length; i++) {
      sb.append(Arrays.toString(higher[i])).append("\n");
    }
    return sb.toString();
  }

  @Override
  public Set<Item> getHigher(Item item) {
    Set<Item> set = new HashSet<Item>();
    int id = item.getId();
    for (int i = 0; i < higher.length; i++) {
      if (higher[i][id]) {
        set.add(items.get(i));
      }
    }
    return set;
  }

  @Override
  public Set<Item> getLower(Item item) {
    Set<Item> set = new HashSet<Item>();
    int id = item.getId();
    for (int i = 0; i < higher.length; i++) {
      if (higher[id][i]) {
        set.add(items.get(i));
      }
    }
    return set;
  }

  /**
   * Create ranking from the items in the collection, if possible
   */
  @Override
  public Ranking toRanking(Collection<Item> items) {
    return this.transitiveClosure().buildRanking(items);
  }
    
    
  private Ranking buildRanking(Collection<Item> items) {  
    Map<Item, Integer> itemCount = new HashMap<Item, Integer>();
    for (Item item : items) {
      itemCount.put(item, 0);
    }
    List<Item> itemList = new ArrayList<Item>(items);

    for (int i = 0; i < itemList.size() - 1; i++) {
      Item it1 = itemList.get(i);
      for (int j = i + 1; j < itemList.size(); j++) {
        Item it2 = itemList.get(j);
        Boolean b = this.isPreferred(it1, it2);
        if (b == null) {
          return null;
        }
        if (b) {
          int c = itemCount.get(it2);
          itemCount.put(it2, c + 1);
        } else {
          int c = itemCount.get(it1);
          itemCount.put(it1, c + 1);
        }
      }
    }

    Map<Integer, Item> reverse = new HashMap<Integer, Item>();
    for (Item it : itemCount.keySet()) {
      reverse.put(itemCount.get(it), it);
    }

    Ranking top = new Ranking(getItemSet());
    for (int i = 0; i < itemList.size(); i++) {
      Item it = reverse.get(i);
      if (it == null) {
        return null;
      }
      top.add(it);
    }
    return top;
  }

  
  @Override
  public DensePreferenceSet project(Collection<Item> items) {
    DensePreferenceSet projection = new DensePreferenceSet(this);
    for (Preference pref: this.getPreferences()) {
      if (items.contains(pref.higher) && items.contains(pref.lower)) projection.add(pref.higher, pref.lower);
    }
    return projection;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof DensePreferenceSet) {
      DensePreferenceSet prefs = (DensePreferenceSet) o;
      if (!prefs.getItemSet().equals(this.getItemSet())) {
        return false;
      }

      return equal(this.higher, prefs.higher);
    } else if (o instanceof SparsePreferenceSet) {
      throw new UnsupportedOperationException("ToDo");
    } else {
      return false;
    }
  }

  private Integer hash;

  @Override
  public int hashCode() {
    if (hash == null) {
      hash = 3;
      hash = 79 * hash + java.util.Arrays.deepHashCode(this.higher);
    }
    return hash;
  }

  @Override
  public Boolean remove(int itemId1, int itemId2) {
    Boolean result = isPreferred(itemId1, itemId2);
    this.higher[itemId1][itemId2] = this.higher[itemId2][itemId1] = false;
    return result;
  }

  @Override
  public Boolean remove(Item item1, Item item2) {
    int hid = item1.getId();
    int lid = item2.getId();
    return remove(hid, lid);
  }
  
  @Override
  public boolean remove(Preference pref) {
    boolean contains = this.higher[pref.higher.id][pref.lower.id];
    this.higher[pref.higher.id][pref.lower.id] = false;
    return contains;
  }


  @Override
  public boolean contains(int higherId, int lowerId) {
    return this.higher[higherId][lowerId];
  }

  @Override
  public boolean contains(Item higher, Item lower) {
    return this.contains(higher.getId(), lower.getId());
  }
    
  @Override
  public boolean contains(Preference pref) {
    return contains(pref.higher.id, pref.lower.id);
  }
  
  
  @Override
  public boolean contains(Item item) {
    for (Item it : items) {
      Boolean b = this.isPreferred(item, it);
      if (b != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int size() {
    int c = 0;
    for (int i = 0; i < higher.length; i++) {
      for (int j = 0; j < higher.length; j++) {
        if (higher[i][j]) c++;
      }
    }
    return c;
  }

  @Override
  public Set<Preference> getPreferences() {
    Set<Preference> prefs = new HashSet<Preference>();
    for (int i = 0; i < higher.length; i++) {
      Item h = items.get(i);
      for (int j = 0; j < higher.length; j++) {
        if (higher[i][j]) {
          Preference p = new Preference(h, items.get(j));
          prefs.add(p);
        }
      }
    }
    return prefs;
  }
  
  @Override
  public Set<Item> getItems() {
    Set<Item> items = new HashSet<Item>();
    for (Preference p: this.getPreferences()) {
      items.add(p.higher);
      items.add(p.lower);
    }
    return items;
  }

  @Override
  public boolean remove(Item item) {
    boolean removed = false;
    for (int i = 0; i < higher.length; i++) {
      if (higher[i][item.id]) {
        removed = true;
        higher[i][item.id] = false;
      }
      if (higher[item.id][i]) {
        removed = true;
        higher[item.id][i] = false;
      }
    }
    return removed;
  }


 
}