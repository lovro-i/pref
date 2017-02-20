package edu.drexel.cs.db.db4pref.posterior.concurrent6;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Expands6 {

  private final Expander6 expander;
  
  private List<ConcurrentHashMap<State6, Double>> statess = new ArrayList<>();
  
  public Expands6(Expander6 expander) {
    this.expander = expander;
    for (int i = 0; i < expander.threads * expander.mapsPerThread; i++) {
      ConcurrentHashMap<State6, Double> states = new ConcurrentHashMap<State6, Double>();
      statess.add(states);
    }
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    for (ConcurrentHashMap<State6, Double> states: statess) {
      states.clear();
    }
    statess.get(0).put(new State6(expander), 1d);
  }
  
  public int size() {
    int size = 0;
    for (ConcurrentHashMap<State6, Double> states: statess) {
      size += states.size();
    }
    return size;
  }
  
  public static long adding = 0;
  
  public ConcurrentHashMap<State6, Double> getStates(State6 state) {
    int idx = Math.abs(state.hashCode()) % statess.size();
    if (idx < 0) idx += statess.size();
    return statess.get(idx);
  }
  
  public void add(State6 state, double p) {
    long start = System.currentTimeMillis();
    ConcurrentHashMap<State6, Double> states = getStates(state);
    states.putIfAbsent(state, new Double(0));
    // states.compute(e, (key, value) -> value + p);
    states.merge(state, p, (oldVal, newVal) -> newVal + oldVal);
    adding += System.currentTimeMillis() - start;
  }
 
  
  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (ConcurrentHashMap<State6, Double> states: statess) {
      for (double p: states.values()) sum += p;
    }
    return sum;
  }
  
  public Expander6 getExpander() {
    return this.expander;
  }
  
  static long init = 0;
  
    /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands6 insert(Item item, boolean missing, Workers6 workers) throws InterruptedException {
    Expands6 expands = new Expands6(expander);
    workers.run(statess, expands, item, missing);
    return expands;
  }
  
  
}
