package edu.drexel.cs.db.rank.preference;

import cern.colt.Arrays;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import java.util.HashSet;
import java.util.Set;


public class DensePreferenceSet implements PreferenceSet {

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
  
  public void add(int higher, int lower) {
    this.higher[higher][lower] = true;
    this.higher[lower][higher] = false;
  }
  
  public void add(Item higher, Item lower) {
    int hid = higher.getId();
    int lid = lower.getId();
    this.higher[hid][lid] = true;
    this.higher[lid][hid] = false;
  }

  @Override
  public Boolean isHigher(Item higher, Item lower) {
    int hid = higher.getId();
    int lid = lower.getId();
    if (this.higher[hid][lid]) return true;
    if (this.higher[lid][hid]) return false;
    return null;
  }
  
  @Override
  public Boolean isHigher(int hid, int lid) {
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
  
  public static DensePreferenceSet fromRanking(Ranking r) {
    DensePreferenceSet prefs = new DensePreferenceSet(r.getItemSet());
    for (int i = 0; i < r.size()-1; i++) {
      Item higher = r.get(i);
      for (int j = i+1; j < r.size(); j++) {
        Item lower = r.get(j);
        prefs.add(higher, lower);        
      }      
    }
    return prefs;
  }
  
  
  public static DensePreferenceSet fromTopKRanking(Ranking r) {    
    DensePreferenceSet prefs = fromRanking(r);
    Set<Item> missing = r.getMissingItems();
    for (int i = 0; i < r.size(); i++) {
      Item higher = r.get(i);
      for (Item lower: missing) {
        prefs.add(higher, lower);        
      }      
    }
    return prefs;
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

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 79 * hash + java.util.Arrays.deepHashCode(this.higher);
    return hash;
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
    
    System.out.println(prefs.isHigher(a, c));
    System.out.println(tc.isHigher(a, c));
    
    
    Ranking r = items.getRandomRanking();
    System.out.println(r);
    System.out.println(DensePreferenceSet.fromRanking(r));
    
    Set<Item> lower = tc.getLower(c);
    System.out.println(Arrays.toString(lower.toArray()));
  }

  
}
