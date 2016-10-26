package edu.drexel.cs.db.db4pref.posterior.app;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.posterior.Span;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;


public class StateKey implements Comparable<StateKey> {
  
  /** The owner object */
  private final Expander expander;
  
  /** Number of missing elements at each position */
  int[] miss;
  
  /** Array of known items */
  Item[] items;
  
  
  /** Create the root state */
  public StateKey(Expander expander) {
    this.expander = expander;
    Item first = expander.model.getCenter().get(0);
    if (expander.pref.getItems().contains(first)) {
      this.items = new Item[1];
      this.items[0] = first;
      this.miss = new int[2];
    }
    else {
      this.items = new Item[0];
      this.miss = new int[1];
      this.miss[0] = 1;
    }
  }
  
  /** Crate a state with no missing items */
  private StateKey(Expander expander, Item[] items) {
    this.expander = expander;
    this.items = new Item[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    miss = new int[items.length + 1];
  }
  
  
  /** Create a clone of the state */
  private StateKey(StateKey state) {
    this.expander = state.expander;
    this.items = new Item[state.items.length];
    System.arraycopy(state.items, 0, this.items, 0, items.length);    
    this.miss = new int[state.miss.length];
    System.arraycopy(state.miss, 0, this.miss, 0, miss.length);
  }
  
  
  
  /** Removes the items that won't figure in the future */
  void compact() {
    int step = this.length();
    for (int i = 0; i < items.length; i++) {
      Span span = this.expander.spans.get(items[i]);
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
        i--;
      }
    }
  }
  
  
  public StateData getData() {
    return expander.getData(this);
  }

  
  /** Returns the length of this ranking (missing + fixed) */
  public int length() {
    int len = this.items.length;
    for (int i = 0; i < miss.length; i++) {
      len += miss[i];      
    }
    return len;
  }
  
  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof StateKey)) return false;
    StateKey state = (StateKey) o;
    if (this.miss.length != state.miss.length) return false;
    for (int i = 0; i < miss.length; i++) {
      if (this.miss[i] != state.miss[i]) return false;      
    }
    
    for (int i = 0; i < items.length; i++) {
      if (!this.items[i].equals(state.items[i])) return false;      
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
  
  
  public boolean expand() {
    StateData data = this.getData();
    // if (data == null) return false;

    if (data.children != null) return false;
    // data.children = new HashSet<Child>();
    
    int nextLevel = this.length();
    if (nextLevel == expander.reference.length()) return false;
    Item item = expander.reference.get(nextLevel);
    boolean missing = !expander.pref.getItems().contains(item);
    
    if (missing) this.insertMissing(data, item);
    else this.insert(data, item);
    
    this.propagate(data.p);
    return true;
  }
  
//  public void propagate() {
//    StateData data = expander.getState(this);
//    if (data.children == null) return;
//    
//    // Logger.info("Propagating...");
//    for (Child child: data.children) {
//      double p = data.p * child.p;
//      expander.add(child.state, p);
//      child.state.propagate(p);
//    }
//  }
  
  
  private void propagate(double p) {
    StateData data = expander.getData(this);
    if (data.children == null) return;
    for (Entry<StateKey, Double> child: data.children.entrySet()) {
      double p1 = p * child.getValue();
      expander.add(child.getKey(), p1);
      child.getKey().propagate(p1);
    }
  }
  
  
  /** Expand possible states from this one, if the specified item is missing (can be inserted between any two present items)
   * @param item to insert
   */
  public void insertMissing(StateData data, Item item) {
    int step = expander.referenceIndex.get(item);
    int pos = 0;
    for (int i = 0; i < this.miss.length; i++) {
      StateKey state = new StateKey(this);
      state.miss[i]++;
      
      double p = 0;
      for (int j = 0; j <= this.miss[i]; j++) {
        p += expander.probability(step, pos);
        pos++;
      }
      state.compact();
      // expander.add(state, p * data.p);
      data.addChild(state, p);
      // data.children.add(new Child(state, p));
    }
  }
  
  private Span hilo(Item item) {
    Set<Item> higher = this.expander.tc.getHigher(item);
    Set<Item> lower = this.expander.tc.getLower(item);
    int low = 0;
    int high = items.length;
    for (int j = 0; j < items.length; j++) {
      Item it = items[j];
      if (higher.contains(it)) {
        low = j + 1;
      }
      if (lower.contains(it) && j < high) {
        high = j;
      }
    }
    return new Span(low, high);
  }
  
  
  private void insert(StateData data, Item item) {
    Span hilo = hilo(item);
    for (int i = hilo.from; i <= hilo.to; i++) {
      insertOne(data, item, i);
    }
  }
  
  private void insertOne(StateData data, Item item, int index) {
    int n = miss[index] + 1; // how many are missing before the previous and the next, plus one: the number of different new expand states
        
    
    int posPrev = index - 1;
    for (int i = 0; i < index; i++) {
      posPrev += miss[i];      
    }
    
    // create new array of items, by inserting it after the previous
    Item[] items1 = new Item[items.length + 1];
    for (int i = 0; i < items1.length; i++) {
      if (i < index) items1[i] = items[i];
      else if (i == index) items1[i] = item;
      else items1[i] = items[i - 1];
    }
    
    // create n new expand states with their probabilities    
    for (int i = 0; i < n; i++) {
      StateKey state = new StateKey(expander, items1);
      for (int j = 0; j < state.miss.length; j++) {
        if (j < index) state.miss[j] = this.miss[j];
        else if (j == index) state.miss[j] = i;
        else if (j == index + 1) state.miss[j] = this.miss[index] - i;
        else state.miss[j] = this.miss[j-1];        
      }
      double p = expander.probability(expander.referenceIndex.get(item), posPrev + 1 + i);
      state.compact();
      // expander.add(state, p * data.p);
      // data.children.add(new Child(state, p));
      data.addChild(state, p);
    }
  }

  @Override
  public int compareTo(StateKey state) {
    int diff = this.length() - state.length();
    if (diff != 0) return diff;
    
    diff = this.miss.length - state.miss.length;
    if (diff != 0) return diff;
    
    for (int i = 0; i < miss.length; i++) {
      diff = this.miss[i] - state.miss[i];
      if (diff != 0) return diff;      
    }
    
    for (int i = 0; i < items.length; i++) {
      diff = this.items[i].compareTo(state.items[i]);
      if (diff != 0) return diff;
    }
    return 0;
  }
  
}
