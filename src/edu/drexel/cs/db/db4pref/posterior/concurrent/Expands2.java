package edu.drexel.cs.db.db4pref.posterior.concurrent;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class Expands2 {

  private final Expander2 expander;
  private ConcurrentMap<State2, Double> states = new ConcurrentHashMap<>();
  
  public Expands2(Expander2 expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    states.clear();
    states.put(new State2(expander), 1d);
  }
  
  public int size() {
    return states.size();
  }
  
  public void add(State2 e, double p) {
    states.putIfAbsent(e, new Double(0));
    // states.compute(e, (key, value) -> value + p);
    states.merge(e, p, (oldVal, newVal) -> newVal + oldVal);
  }
 
  
  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (double p: states.values()) sum += p;
    return sum;
  }
  
  public Expander2 getExpander() {
    return this.expander;
  }
  
  static long init = 0;
  
    /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands2 insert(Item item, boolean missing, Workers workers) throws InterruptedException {
    
    // if (item.getTag().equals(26)) {
    //  System.out.println("============================================================================");
    //  Logger.info("Item %s, missing %s, %d threads", item, missing, expander.threads);
    // }
    
    Expands2 expands = new Expands2(expander);
    Queue<Entry<State2, Double>> q = new ArrayDeque<>(states.entrySet());
    workers.run(q, expands, item, missing);
    return expands;
  }
  
  
}
