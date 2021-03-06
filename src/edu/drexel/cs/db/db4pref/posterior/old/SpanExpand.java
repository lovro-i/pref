package edu.drexel.cs.db.db4pref.posterior.old;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.posterior.Sequence;
import edu.drexel.cs.db.db4pref.posterior.Span;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.Arrays;

/** One state in the expansion. The state is represented by the ordering of known items, and number of unknown items between them (plus in front and after).
 * Example: 1.A.2.B.1 is ranking xAxxBx, where x are any items
 */
public class SpanExpand {
  
  /** The owner object */
  private final SpanExpander expander;
  
  /** Number of missing elements at each position */
  private int[] miss;
  
  /** Array of known items */
  private Item[] items;

  
  /** Create the state from a given sequence */
  public SpanExpand(Sequence seq) {
    this.items = new Item[seq.size()];
    this.miss = new int[this.items.length + 1];
    this.expander = null;
    
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
  
  /** Create an empty state */
  public SpanExpand(SpanExpander expander) {
    this.expander = expander;
    this.items = new Item[0];
    this.miss = new int[1];
  }
  
  /** Crate a state with no missing items */
  private SpanExpand(SpanExpander expander, Item[] items) {
    this.expander = expander;
    this.items = new Item[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    miss = new int[items.length + 1];
  }
  
  /** Create a clone of the state */
  private SpanExpand(SpanExpander expander, SpanExpand e) {
    this.expander = expander;
    this.items = new Item[e.items.length];
    System.arraycopy(e.items, 0, this.items, 0, items.length);    
    this.miss = new int[e.miss.length];
    System.arraycopy(e.miss, 0, this.miss, 0, miss.length);
  }
  
  /** Removes the items that won't figure in the future */
  void compact(int step) {
    for (int i = 0; i < items.length; i++) {
      String before = this.toString();
      Span span = expander.spans.get(items[i]);
      if (step > span.to) {
        Item[] items2 = new Item[items.length-1];
        int[] miss2 = new int[miss.length-1];
        for (int j = 0; j < items2.length; j++) {
          if (j < i) items2[j] = items[j];
          else items2[j] = items[j+1];
        }
        for (int j = 0; j < miss2.length; j++) {
          if (j < i) miss2[j] = miss[j];
          else if (j == i) miss2[j] = miss[j] + miss[j+1] + 1;
          else miss2[j] = miss[j+1];
        }
        
        this.items = items2;
        this.miss = miss2;
        // Logger.info("Compacting at step %d: %s -> %s", step, before, this);
        i--;
      }
    }
  }
  
  
  
  /** Returns the length of this ranking (missing + fixed) */
  public int length() {
    int len = this.items.length;
    for (int i = 0; i < miss.length; i++) {
      len += miss[i];      
    }
    return len;
  }
  
  /** Expand possible states from this one, if the specified item is missing (can be inserted between any two present items)
   * @param item to insert
   * @return Mapping of states to their probabilities
   */
  public SpanExpands insertMissing(Item item) {
    SpanExpands expands = new SpanExpands(expander);
    int step = expander.referenceIndex.get(item);
    SpanExpand exc = new SpanExpand(expander, this);
    exc.compact(step);
    int pos = 0;
    for (int i = 0; i < exc.miss.length; i++) {
      SpanExpand ex = new SpanExpand(expander, exc);
      ex.miss[i]++;
      
      double p = 0;
      for (int j = 0; j <= exc.miss[i]; j++) {
        p += probability(step, pos);
        pos++;
      }
      // ex.compact(step);
      expands.add(ex, p);
    }
    // expands.normalize();
    return expands;
  }
  

  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  private double probability(int itemIndex, int position) {
    double phi = expander.getModel().getPhi();
    double p = Math.pow(phi, Math.abs(itemIndex - position)) * (1 - phi) / (1 - Math.pow(phi, itemIndex + 1));
    return p;
  }
  
  
  /** Adds item e to the right of the item 'prev'.
   *  If (prev == null), it is added at the beginning
   */  
  public SpanExpands insert(Item e, Item prev) {
    SpanExpands expands = new SpanExpands(expander);
    int step = expander.referenceIndex.get(e);
    this.compact(step);
    
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
      SpanExpand ex = new SpanExpand(expander, items1);
      for (int j = 0; j < ex.miss.length; j++) {
        if (j <= index) ex.miss[j] = this.miss[j];
        else if (j == index + 1) ex.miss[j] = i;
        else if (j == index + 2) ex.miss[j] = this.miss[index + 1] - i;
        else ex.miss[j] = this.miss[j-1];        
      }
      double p = probability(step, posPrev + 1 + i);
      // ex.compact(step);
      expands.put(ex, p);
    }
    
    // expands.normalize(); // treba
    return expands;
  }
  
  /** @return Index of item e in the array of fixed items */
  private int indexOf(Item e) {
    if (e == null) return -1;
    for (int i = 0; i < items.length; i++) {
      if (e.equals(items[i])) return i;      
    }
    return -1;
  }
  
  /** @return Index of item e in the array of all (fixed + missed) items */
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
    if (!(o instanceof SpanExpand)) return false;
    SpanExpand e = (SpanExpand) o;
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
