package edu.drexel.cs.db.rank.top;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.triangle.TriangleRow;
import java.util.Arrays;

public class TopExpand {
  
  private int[] miss;
  private Item[] items;

  public TopExpand() {
    this.items = new Item[0];
    this.miss = new int[1];
  }
  
  private TopExpand(Item[] items) {
    this.items = new Item[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    miss = new int[items.length + 1];
  }
  
  private TopExpand(TopExpand e) {
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
  
  public TopExpands insertMissing() {
    TopExpands expands = new TopExpands();
    TopExpand ex = new TopExpand(this);
    ex.miss[miss.length-1]++;      
    expands.add(ex, 1d);
    return expands;
  }

  
  /** Adds item e to the right of the item 'prev'.
   *  If (after == null), it is added at the beginning
   */  
  public TopExpands insert(Item e, Item prev) {
    TopExpands expands = new TopExpands();
    
    int index = indexOf(prev);        
    
    
    Item[] items1 = new Item[items.length + 1];
    for (int i = 0; i < items1.length; i++) {
      if (i <= index) items1[i] = items[i];
      else if (i == index + 1) items1[i] = e;
      else items1[i] = items[i - 1];
    }
    
    TopExpand ex = new TopExpand(items1);
    for (int j = 0; j < ex.miss.length; j++) {
      if (j <= index) ex.miss[j] = this.miss[j];
      else if (j == index + 1) ex.miss[j] = 0;
      else if (j == index + 2) ex.miss[j] = this.miss[index + 1];
      else ex.miss[j] = this.miss[j-1];        
    }
    expands.put(ex, 1d);
    
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
  
  public boolean equals(Object o) {
    if (!(o instanceof TopExpand)) return false;
    TopExpand e = (TopExpand) o;
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
  

  /** @return is Item e at position pos */
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
