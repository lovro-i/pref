package edu.drexel.cs.db.db4pref.posterior.sequential2;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.posterior.Sequence;
import edu.drexel.cs.db.db4pref.posterior.Span;
import edu.drexel.cs.db.db4pref.posterior.expander.State;
import java.util.Set;


public class State2 extends State {

  
  /** Create the state from a given sequence */
  public State2(Sequence seq) {
    super(null);
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
  
  /** Create an empty state
   * @param expander */
  public State2(Expander2 expander) {
    this.expander = expander;
    this.items = new Item[0];
    this.miss = new int[1];
  }
  
  public State2(Expander2 expander, Item[] items, int[] miss) {
    super(expander, items, miss);
  }
  
  public State2(Expander2 expander, int[] itemIds, int[] miss) {
    super(expander, itemIds, miss);
  }
  
  
  public Ranking getRanking() {
    Ranking ranking = new Ranking(expander.getModel().getItemSet());
    for (Item item: items) ranking.add(item);
    return ranking;
  }
  
  /** Crate a state with no missing items */
  private State2(Expander2 expander, Item[] items) {
    this.expander = expander;
    this.items = new Item[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    miss = new int[items.length + 1];
  }
  
  /** Create a clone of the state */
  private State2(Expander2 expander, State2 e) {
    this.expander = expander;
    this.items = new Item[e.items.length];
    System.arraycopy(e.items, 0, this.items, 0, items.length);    
    this.miss = new int[e.miss.length];
    System.arraycopy(e.miss, 0, this.miss, 0, miss.length);
  }
  
  
  public State2 clone() {
   return new State2((Expander2) expander, this); 
  }
  
  
  /** Expand possible states from this one, if the specified item is missing (can be inserted between any two present items)
   * @param item to insert
   * @return Mapping of states to their probabilities
   */
  public void insertMissing(Expands2 expands, Item item, double p1) {
    int step = expander.getReferenceIndex(item);
    int pos = 0;
    for (int i = 0; i < this.miss.length; i++) {
      State2 state = this.clone();
      state.miss[i]++;
      
      double p = 0;
      for (int j = 0; j <= this.miss[i]; j++) {
        p += expander.probability(step, pos);
        pos++;
      }
      state.compact();
      expands.add(state, p * p1);
    }
    ((Expander2) expander).incStates(miss.length);
  }
  
  private void insertOneMissing(Expands2 expands, int step, int idx, double p1) {
    int pos = 0;
    for (int i = 0; i < idx; i++) {
      pos += miss[i] + 1;
    }

    State2 state = this.clone();
    state.miss[idx]++;

    double p = 0;
    for (int j = 0; j <= this.miss[idx]; j++) {
      p += expander.probability(step, pos);
      pos++;
    }
    state.compact();
    expands.add(state, p * p1);
  }
  
  public void insert(Expands2 expands, Item item, boolean missing, double p) {
    if (missing) this.insertMissing(expands, item, p);
    else this.insertPresent(expands, item, p);
  }
  
  
  
  
  public Span hilo(Item item) {
    Set<Item> higher = this.expander.getTransitiveClosure().getHigher(item);
    Set<Item> lower = this.expander.getTransitiveClosure().getLower(item);
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
  
  
  protected State2 createState(Item[] items) {
    return new State2((Expander2) expander, items);
  }
  
  
  private void insertOne(Expands2 expands, Item item, int index, double p1) {
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
      State2 state = this.createState(items1);
      for (int j = 0; j < state.miss.length; j++) {
        if (j < index) state.miss[j] = this.miss[j];
        else if (j == index) state.miss[j] = i;
        else if (j == index + 1) state.miss[j] = this.miss[index] - i;
        else state.miss[j] = this.miss[j-1];        
      }
      double p = expander.probability(expander.getReferenceIndex(item), posPrev + 1 + i);
      state.compact();
      expands.add(state, p * p1);
    }
    ((Expander2) expander).incStates(n);
  }

  
  public void insertPresent(Expands2 expands, Item item, double p1) {
    Span track = expander.getSpan(item);
    if (track.from == track.to) {
      insertNonTracked(expands, item, p1);
    }
    else {
      Span hilo = hilo(item);
      for (int i = hilo.from; i <= hilo.to; i++) {
        insertOne(expands, item, i, p1);
      }
    }
  }
  
  public void insertNonTracked(Expands2 expands, Item item, double p1) {
    int step = expander.getReferenceIndex(item);
    Span hilo = hilo(item);
    for (int i = hilo.from; i <= hilo.to; i++) {
      this.insertOneMissing(expands, step, i, p1);
    }
    ((Expander2) expander).incStates(hilo.to - hilo.from + 1);
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
    if (!(o instanceof State2)) return false;
    State2 e = (State2) o;
    if (this.miss.length != e.miss.length) return false;
    for (int i = 0; i < miss.length; i++) {
      if (this.miss[i] != e.miss[i]) return false;      
    }
    
    for (int i = 0; i < items.length; i++) {
      if (!this.items[i].equals(e.items[i])) return false;      
    }
    
    return true;
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
