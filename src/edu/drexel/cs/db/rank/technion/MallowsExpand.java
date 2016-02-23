package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.Arrays;

/** One state */
public class MallowsExpand {
  
  private Expander expander;
  private int[] miss;
  private Item[] items;

  public MallowsExpand(Sequence seq) {
    this.items = new Item[seq.size()];
    this.miss = new int[this.items.length + 1];
    
    int ie = 0;
    int im = 0;
    Item[] s = seq.getItems();
    for (int i = 0; i < s.length; i++) {
      if (s[i] == null) this.miss[im]++;
      else {
        this.items[ie] = s[i];
        ie++;
        im++;
      }
    }
  }
  
  public MallowsExpand(Expander expander) {
    this.expander = expander;
    this.items = new Item[0];
    this.miss = new int[1];
  }
  
  private MallowsExpand(Expander expander, Item[] items) {
    this.expander = expander;
    this.items = new Item[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    miss = new int[items.length + 1];
  }
  
  private MallowsExpand(Expander expander, MallowsExpand e) {
    this.expander = expander;
    this.items = new Item[e.items.length];
    System.arraycopy(e.items, 0, this.items, 0, items.length);    
    this.miss = new int[e.miss.length];
    System.arraycopy(e.miss, 0, this.miss, 0, miss.length);
  }
  
  /** Returns the length of this ranking (missing + fixed) */
  public int length() {
    int len = this.items.length;
    for (int i = 0; i < miss.length; i++) {
      len += miss[i];      
    }
    return len;
  }
  
  public MallowsExpands insertMissing(Item e) {
    MallowsExpands expands = new MallowsExpands(expander);
    
    int pos = 0;
    for (int i = 0; i < miss.length; i++) {
      MallowsExpand ex = new MallowsExpand(expander, this);
      ex.miss[i]++;      
      
      double p = 0;
      for (int j = 0; j <= miss[i]; j++) {
        p += probability(e.getId(), pos);
        pos++;
      }
      expands.add(ex, p);
    }
    // expands.normalize();
    return expands;
  }
  

  private double probability(int itemIndex, int position) {
    double phi = expander.getModel().getPhi();
    double r = Math.pow(phi, Math.abs(itemIndex - position));
    return r;
  }
  
  
  /** Adds item e to the right of the item 'prev'.
   *  If (after == null), it is added at the beginning
   */  
  public MallowsExpands insert(Item e, Item prev) {
    MallowsExpands expands = new MallowsExpands(expander);
    
    int index = indexOf(prev); // index of the previous item
    int n = miss[index + 1] + 1; // how many are missing before the previous and the next, plus one: the number of different new expand states
        
    
    int posPrev = index;
    for (int i = 0; i <= index; i++) {
      posPrev += miss[i];      
    }
    
    // create new array of items, by inserting it after the previous
    Item[] items1 = new Item[items.length + 1];
    for (int i = 0; i < items1.length; i++) {
      if (i <= index) items1[i] = items[i];
      else if (i == index + 1) items1[i] = e;
      else items1[i] = items[i - 1];
    }
    
    // create n new expand states with their probabilities    
    for (int i = 0; i < n; i++) {
      MallowsExpand ex = new MallowsExpand(expander, items1);
      for (int j = 0; j < ex.miss.length; j++) {
        if (j <= index) ex.miss[j] = this.miss[j];
        else if (j == index + 1) ex.miss[j] = i;
        else if (j == index + 2) ex.miss[j] = this.miss[index + 1] - i;
        else ex.miss[j] = this.miss[j-1];        
      }
      double p = probability(e.getId(), posPrev + 1 + i);
      expands.put(ex, p);
    }
    
    // expands.normalize(); // treba
    return expands;
  }
  
  /** @returns Index of item e in the array of fixed items */
  private int indexOf(Item e) {
    if (e == null) return -1;
    for (int i = 0; i < items.length; i++) {
      if (e.equals(items[i])) return i;      
    }
    return -1;
  }
  
  /** @returns Index of item e in the array of all (fixed + missed) items */
  public int position(Item e) {
    if (e == null) return -1;
    int pos = 0;
    for (int i = 0; i < items.length; i++) {
      pos += miss[i];
      if (e.equals(items[i])) return pos;
      pos++;
    }
    return -1;
  }
  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MallowsExpand)) return false;
    MallowsExpand e = (MallowsExpand) o;
    if (this.miss.length != e.miss.length) return false;
    for (int i = 0; i < miss.length; i++) {
      if (this.miss[i] != e.miss[i]) return false;      
    }
    
    for (int i = 0; i < items.length; i++) {
      if (!this.items[i].equals(e.items[i])) return false;      
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 73 * hash + Arrays.hashCode(this.miss);
    hash = 73 * hash + Arrays.deepHashCode(this.items);
    return hash;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();    
    sb.append(miss[0]);
    for (int i = 0; i < items.length; i++) {
      sb.append('.').append(items[i]);
      sb.append('.').append(miss[i+1]);      
    }
    return sb.toString();
  }
  

  /** @return Is Item e at position pos */
  public boolean isAt(Item e, int pos) {
    int i = 0;
    for (int j = 0; j < items.length; j++) {
      i += miss[j];
      if (e.equals(items[i])) return i == pos;
      i++;
    }
    return false;
  }
  
}
