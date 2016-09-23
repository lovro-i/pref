package edu.drexel.cs.db.db4pref.posterior.sequential;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.TimeoutException;


public class Expands1 {

  private final Expander1 expander;
  private Map<State1, Double> states = new HashMap<>();
  
  public Expands1(Expander1 expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    states.clear();
    states.put(new State1(expander), 1d);
  }
  
  public void add(State1 e, double p) {
    Double prev = states.get(e);
    if (prev != null) p += prev;
    states.put(e, p);
  }
  
  public void put(State1 e, double p) {
    states.put(e, p);
  }
  
  /** Adds all the Expands to this one with weight p */
  void add(Expands1 expands, double p) {
    for (State1 e: expands.states.keySet()) {
      double v = expands.states.get(e);
      this.add(e, p * v);
    }
  }
  
  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (double p: states.values()) sum += p;
    return sum;
  }
  
    /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands1 insert(Item item, boolean missing) throws InterruptedException {
    Expands1 expands = new Expands1(expander);
    for (Entry<State1, Double> entry: states.entrySet()) {
      State1 state = entry.getKey();
      double p = entry.getValue();
      state.insert(expands, item, missing, p);
    }
    return expands;
  }
  
}
