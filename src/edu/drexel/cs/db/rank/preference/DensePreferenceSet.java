package edu.drexel.cs.db.rank.preference;

import cern.colt.Arrays;
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


public class DensePreferenceSet implements MutablePreferenceSet {

  private final ItemSet items;
  private final boolean[][] higher;
  
  public DensePreferenceSet(ItemSet items) {
    this.items = items;
    higher = new boolean[items.size()][items.size()];
  }
  
  private DensePreferenceSet(ItemSet items, boolean[][] a) {
    this.items = items;
    this.higher = a;
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
  
  @Override
  public boolean add(int higher, int lower) {
    if (this.higher[higher][lower] && !this.higher[lower][higher]) return false;
    this.higher[higher][lower] = true;
    this.higher[lower][higher] = false;
    return true;
  }
  
  @Override
  public boolean add(Item higher, Item lower) {    
    int hid = higher.getId();
    int lid = lower.getId();
    return add(hid, lid);
  }

  @Override
  public Boolean isPreferred(Item preferred, Item over) {
    int hid = preferred.getId();
    int lid = over.getId();
    if (this.higher[hid][lid]) return true;
    if (this.higher[lid][hid]) return false;
    return null;
  }
  
  @Override
  public Boolean isPreferred(int hid, int lid) {
    if (this.higher[hid][lid]) return true;
    if (this.higher[lid][hid]) return false;
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
    }
    while (!eq);
    
    return new DensePreferenceSet(items, c);    
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
        if (a[i][j] != b[i][j]) return false;
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
      if (higher[i][id]) set.add(items.get(i));
    }
    return set;
  }

  @Override
  public Set<Item> getLower(Item item) {
    Set<Item> set = new HashSet<Item>();
    int id = item.getId();
    for (int i = 0; i < higher.length; i++) {
      if (higher[id][i]) set.add(items.get(i));
    }
    return set;
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
      Item it = reverse.get(i);
      if (it == null) return null;
      top.add(it);
    }    
    return top;
  }
  
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof DensePreferenceSet) {
      DensePreferenceSet prefs = (DensePreferenceSet) o;
      if (!prefs.getItemSet().equals(this.getItemSet())) return false;

      return equal(this.higher, prefs.higher);
    }
    else if (o instanceof SparsePreferenceSet) {
      throw new UnsupportedOperationException("ToDo");
    }
    else {
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
  public boolean contains(int higherId, int lowerId) {
    return this.higher[higherId][lowerId];
  }
  
  @Override
  public boolean contains(Item higher, Item lower) {
    return this.contains(higher.getId(), lower.getId());
  }
  
  @Override
  public boolean contains(Item item) {
    for (Item it: items) {
      Boolean b = this.isPreferred(item, it);
      if (b != null) return true;
    }
    return false;
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(4);
    DensePreferenceSet prefs = new DensePreferenceSet(items);
    Item a = items.get(0);
    Item b = items.get(1);
    Item c = items.get(2);
    Item d = items.get(3);
    prefs.add(a, b);
    prefs.add(b, c);
    prefs.add(b, d);
    
    DensePreferenceSet tc = prefs.transitiveClosure();
    System.out.println(prefs);
    System.out.println("\ntc:");
    System.out.println(tc);
    System.out.println("");
    
    System.out.println(prefs.isPreferred(a, c));
    System.out.println(tc.isPreferred(a, c));
    
    
    Ranking r = items.getRandomRanking();
    System.out.println(r);
    System.out.println(r.transitiveClosure());
    
    Set<Item> lower = tc.getLower(c);
    System.out.println(Arrays.toString(lower.toArray()));
  }
}
