package edu.drexel.cs.db.db4pref.posterior.concurrent3;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class Expands3 {

  private final Expander3 expander;
  private ConcurrentMap<State3, Double> states = new ConcurrentHashMap<>();
  
  public Expands3(Expander3 expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    states.clear();
    states.put(new State3(expander), 1d);
  }
  
  public int size() {
    return states.size();
  }
  
  public void add(State3 e, double p) {
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
  
  public Expander3 getExpander() {
    return this.expander;
  }
  
  static long init = 0;
  
    /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands3 insert(Item item, boolean missing, Workers3 workers) throws InterruptedException {
    Expands3 expands = new Expands3(expander);
    workers.run(states.entrySet(), expands, item, missing);
    return expands;
  }
  
  
}
