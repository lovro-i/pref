package edu.drexel.cs.db.db4pref.posterior.concurrent4;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class Expands4 {

  private final Expander4 expander;
  private ConcurrentHashMap<State4, Double> states = new ConcurrentHashMap<>();
  
  public Expands4(Expander4 expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    states.clear();
    states.put(new State4(expander), 1d);
  }
  
  public int size() {
    return states.size();
  }
  
  public static long adding = 0;
  
  public void add(State4 e, double p) {
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
  
  public Expander4 getExpander() {
    return this.expander;
  }
  
  static long init = 0;
  
    /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands4 insert(Item item, boolean missing, Workers4 workers) throws InterruptedException {
    Expands4 expands = new Expands4(expander);
    workers.run(states, expands, item, missing);
    return expands;
  }
  
  
}
