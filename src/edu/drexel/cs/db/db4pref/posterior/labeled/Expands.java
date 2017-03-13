/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.posterior.labeled;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author hping
 */
public class Expands {

  private final Expander expander;
  Map<State, Double> states = new HashMap<>();

  public Expands(Expander expander) {
    this.expander = expander;
  }

  public void nullify() {
    states.clear();
    states.put(new State(expander), 1d);
  }

  public void put(State state, Double p) {
    states.put(state, p);
  }

  public Expands insert(int step) {
    Item item = expander.getModel().getCenter().get(step);
    boolean missing = !expander.getPresentItems().contains(item);
//    System.out.format("step=%d, missing=%b\n", step, missing);
    // TODO add latest parent detection
    Expands expands = new Expands(expander);
    for (Entry<State, Double> entry : states.entrySet()) {
      State state = entry.getKey();
      double p = entry.getValue();
      state.insert(expands, item, missing, p);
    }
    return expands;
  }

  public void add(State s, double p) {
    if (states.containsKey(s)) {
      states.put(s, states.get(s) + p);
    } else {
      states.put(s, p);
    }
  }

  public double getProbability() {
    double sum = 0;
    for (Double p : states.values()) {
      sum += p;
    }
    return sum;
  }
}
