package edu.drexel.cs.db.db4pref.posterior.sequential2;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class Expands2 {

  private final Expander2 expander;
  Map<State2, Doubler> states = new HashMap<>();
  
  
  public Expands2(Expander2 expander) {
    this.expander = expander;
  }
  
  public Map<State2, Doubler> getStates() {
    return states;
  }
  
  public Expands2 compact() {
    Expands2 expands = new Expands2(expander);
    for (Entry<State2, Doubler> entry: states.entrySet()) {
      State2 state = entry.getKey();
      state.compact();
      expands.add(state, entry.getValue().get());
    }
    // Logger.info("Expands compacted from %d to %d", this.size(), expands.size());
    return expands;
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
    Doubler prev = states.get(e);
    if (prev == null) states.put(e, new Doubler(p));
    else prev.add(p);
  }
  
  public void put(State2 e, double p) {
    Doubler d = states.get(e);
    if (d == null) states.put(e, new Doubler(p));
    else d.set(p);
  }
  
  
  
  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (Doubler p: states.values()) sum += p.get();
    return sum;
  }
  
  public Expands2 insert(int step) {
    Item item = expander.getModel().getCenter().get(step);
    boolean missing = !expander.getPreferenceSet().contains(item);
    Expands2 expands = new Expands2(expander);
    for (Entry<State2, Doubler> entry: states.entrySet()) {
      State2 state = entry.getKey();
      double p = entry.getValue().get();
      state.insert(expands, item, missing, p);
    }
    return expands;
  }
  
  
}
