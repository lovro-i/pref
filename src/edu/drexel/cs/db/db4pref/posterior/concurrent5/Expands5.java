package edu.drexel.cs.db.db4pref.posterior.concurrent5;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.concurrent.ConcurrentHashMap;


public class Expands5 {

  private final Expander5 expander;
  private ConcurrentHashMap<State5, Double> states = new ConcurrentHashMap<>();
  
  public Expands5(Expander5 expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    states.clear();
    states.put(new State5(expander), 1d);
  }
  
  public int size() {
    return states.size();
  }
  
  public static long adding = 0;
  
  public void add(State5 e, double p) {
    long start = System.currentTimeMillis();
    states.putIfAbsent(e, new Double(0));
    // states.compute(e, (key, value) -> value + p);
    states.merge(e, p, (oldVal, newVal) -> newVal + oldVal);
    adding += System.currentTimeMillis() - start;
  }
 
  
  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (double p: states.values()) sum += p;
    return sum;
  }
  
  public Expander5 getExpander() {
    return this.expander;
  }
  
  static long init = 0;
  
    /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands5 insert(Item item, boolean missing, Workers5 workers) throws InterruptedException {
    Expands5 expands = new Expands5(expander);
    workers.run(states, expands, item, missing);
    return expands;
  }
  
  
}
