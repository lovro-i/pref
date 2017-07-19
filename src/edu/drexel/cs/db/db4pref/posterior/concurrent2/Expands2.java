package edu.drexel.cs.db.db4pref.posterior.concurrent2;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.DoubleAdder;

public class Expands2 implements Cloneable {

  private final Expander2 expander;
  private ConcurrentMap<State2, DoubleAdder> states = new ConcurrentHashMap<>();

  public Expands2(Expander2 expander) {
    this.expander = expander;
  }

  /**
   * Clear this Expands so that it contains only null (empty) expansion
   */
  public void nullify() {
    states.clear();
    DoubleAdder one = new DoubleAdder();
    one.add(1d);
    states.put(new State2(expander), one);
  }

  public int size() {
    return states.size();
  }

  public void add(State2 e, double p) {
    states.putIfAbsent(e, new DoubleAdder());
    // states.compute(e, (key, value) -> value + p);
    // states.merge(e, p, (oldVal, newVal) -> newVal + oldVal);
    states.get(e).add(p);
  }

  /**
   * Returns the total probability of all the expanded states
   */
  public double getProbability() {
    double sum = 0;
    for (DoubleAdder p : states.values()) {
      sum += p.doubleValue();
    }
    return sum;
  }

  public Expander2 getExpander() {
    return this.expander;
  }

  static long init = 0;

  /**
   * Expand possible states if the specified item is missing (can be inserted
   * between any two present items)
   *
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands2 insert(Item item, boolean missing, boolean isLowerBound, Workers2 workers) throws InterruptedException {

    // if (item.getTag().equals(26)) {
    //  System.out.println("============================================================================");
    //  Logger.info("Item %s, missing %s, %d threads", item, missing, expander.threads);
    // }
    Expands2 expands = new Expands2(expander);
    workers.run(states.entrySet(), expands, item, missing, isLowerBound);
    return expands;
  }

  /**
   * Length of the states in this expander
   */
  public int length() {
    State2 state = states.keySet().iterator().next();
    return state.length();
  }
  
  public ConcurrentMap<State2, DoubleAdder> getStates() {
    return states;
  }
  
  public Expands2 clone() {
    Expands2 expands = new Expands2(expander);
    for (State2 state: states.keySet()){
      expands.add(state.clone(), states.get(state).doubleValue());
    }
    return expands;
  }
}
