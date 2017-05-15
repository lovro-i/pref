package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.posterior.Span;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class State implements Cloneable {
  
    /** The owner object */
  protected Expander expander;
  
  /** Number of missing elements at each position */
  protected int[] miss;
  
  /** Array of known items */
  protected Item[] items;
  
  
  protected State() {
  }
  
    
  /** Create the root state */
  public State(Expander expander) {
    this.expander = expander;
    Item first = expander.getModel().getCenter().get(0);
    if (expander.getPreferenceSet().getItems().contains(first)) {
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
  
  public State(Expander expander, Item[] items, int[] miss) {
    this.expander = expander;
    if (items.length != miss.length - 1) throw new IllegalArgumentException("Array lengths not compatible");
    this.items = items;
    this.miss = miss;
  }
  
  public State(Expander expander, int[] itemIds, int[] miss) {
    if (itemIds.length != miss.length - 1) throw new IllegalArgumentException("Array lengths not compatible");
    this.expander = expander;
    this.miss = miss;
    this.items = new Item[itemIds.length];
    for (int i = 0; i < items.length; i++) {
      items[i] = expander.getModel().getItemSet().getItemById(itemIds[i]);
    }
  }
  
  /** Crate a state with no missing items */
  protected State(Expander expander, Item[] items) {
    this.expander = expander;
    this.items = new Item[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    miss = new int[items.length + 1];
  }
  
  
  /** Create a clone of the state */
  private State(State state) {
    this.expander = state.expander;
    this.items = new Item[state.items.length];
    System.arraycopy(state.items, 0, this.items, 0, items.length);    
    this.miss = new int[state.miss.length];
    System.arraycopy(state.miss, 0, this.miss, 0, miss.length);
  }
  
  
   
  /** Removes the items that won't figure in the future */
  public void compact() {
    int step = this.length();
    for (int i = 0; i < items.length; i++) {
      Span span = this.expander.getSpan(items[i]);
      if (span == null || step > span.to) {
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
    if (!(o instanceof State)) return false;
    State state = (State) o;
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
    int hash = 511 + Arrays.hashCode(this.miss);
    hash = 73 * hash + Arrays.hashCode(this.items);
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
  
  
  
  public Map<State, Double> expand() {
    int nextLevel = this.length();
    Item item = expander.getModel().getCenter().get(nextLevel);
    boolean missing = !expander.getPreferenceSet().getItems().contains(item);
    
    if (missing) return this.insertMissing(item);
    else return this.insert(item);
  }
  

  
  /** Expand possible states from this one, if the specified item is missing (can be inserted between any two present items)
   * @param item to insert
   */
  public Map<State, Double> insertMissing(Item item) {
    Map<State, Double> children = new HashMap<>();
    int step = expander.getReferenceIndex(item);
    int pos = 0;
    for (int i = 0; i < this.miss.length; i++) {
      State state = this.clone();
      state.miss[i]++;
      
      double p = 0;
      for (int j = 0; j <= this.miss[i]; j++) {
        p += expander.probability(step, pos);
        pos++;
      }
      state.compact();
      children.put(state, p);
      // Logger.info("%s -m-> %s: %f", this, state, p);
    }
    return children;
  }
  
  private Span hilo(Item item) {
    Set<Item> higher = this.expander.getTransitiveClosure().getHigher(item);
    Set<Item> lower = this.expander.getTransitiveClosure().getLower(item);
    int low = 0;
    int high = items.length;
    for (int j = 0; j < items.length; j++) {
      Item it = items[j];
      if (higher.contains(it)) {
        low = j + 1;
      }
      if (j < high && lower.contains(it)) {
        high = j;
      }
    }
    return new Span(low, high);
  }
  
  
  private Map<State, Double> insert(Item item) {
    Map<State, Double> children = new HashMap<>();
    Span hilo = hilo(item);
    for (int i = hilo.from; i <= hilo.to; i++) {
      insertOne(children, item, i);
    }
    return children;
  }
  
  /** Create a state with no missing items */
  protected abstract State createState(Item[] items);
  
  @Override
  protected abstract State clone();
  
  private void insertOne(Map<State, Double> children, Item item, int index) {
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
      State state = createState(items1);
      for (int j = 0; j < state.miss.length; j++) {
        if (j < index) state.miss[j] = this.miss[j];
        else if (j == index) state.miss[j] = i;
        else if (j == index + 1) state.miss[j] = this.miss[index] - i;
        else state.miss[j] = this.miss[j-1];        
      }
      double p = expander.probability(expander.getReferenceIndex(item), posPrev + 1 + i);
      state.compact();
      
      if (children.containsKey(state)) {
        double p1 = children.get(state);
        children.put(state, p + p1);
      }
      else {
        children.put(state, p);
      }
      // Logger.info("%s -> %s: %f", this, state, p);
    }
  }

  /**  */
  public boolean contains(Item item) {
    for (Item it: this.items) {
      if (it.equals(item)) return true;
    }
    return false;
  }
  
  /* Position of item in the state, -1 if not present */
  public int indexOf(Item item) {
    int c = 0;
    for (int i = 0; i < items.length; i++) {
      c += miss[i];
      if (items[i].equals(item)) return c;
      c++;
    }
    return -1;
  }
  
  /** Convert to list of Items where null represents untracked items */ 
  public List<Item> toList() {
    List<Item> list = new ArrayList<Item>();
    for (int i = 0; i < miss.length; i++) {
      for (int j = 0; j < miss[i]; j++) list.add(null);
      if (i < items.length) list.add(items[i]);
    }
    return list;
  }
}
