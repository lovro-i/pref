package edu.drexel.cs.db.db4pref.posterior.concurrent2;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.posterior.sequential2.Doubler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class Expands2 {

  private final Expander2 expander;
  private ConcurrentMap<State2, Doubler> states = new ConcurrentHashMap<>();
  
  public Expands2(Expander2 expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    states.clear();
    states.put(new State2(expander), new Doubler(1d));
  }
  
  public int size() {
    return states.size();
  }
  
  public void add(State2 e, double p) {
    states.putIfAbsent(e, new Doubler(0));
    // states.compute(e, (key, value) -> value + p);
    // states.merge(e, p, (oldVal, newVal) -> newVal + oldVal);
    states.get(e).add(p);
  }
 
  
  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (Doubler p: states.values()) sum += p.get();
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
  public Expands2 insert(Item item, boolean missing, Workers2 workers) throws InterruptedException {
    
    // if (item.getTag().equals(26)) {
    //  System.out.println("============================================================================");
    //  Logger.info("Item %s, missing %s, %d threads", item, missing, expander.threads);
    // }
    
    Expands2 expands = new Expands2(expander);
    workers.run(states.entrySet(), expands, item, missing);
    return expands;
  }
  
  
}
