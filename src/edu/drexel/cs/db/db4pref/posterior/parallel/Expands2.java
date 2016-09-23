package edu.drexel.cs.db.db4pref.posterior.parallel;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;


public class Expands2 {

  private final Expander2 expander;
  private Map<State2, Double> states = new HashMap<>();
  
  public Expands2(Expander2 expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    states.clear();
    states.put(new State2(expander), 1d);
  }
  
  public void add(State2 e, double p) {
    Double prev = states.get(e);
    if (prev != null) p += prev;
    states.put(e, p);
  }
  
  public void add(Expands2 expands) {
    for (Entry<State2, Double> entry: expands.states.entrySet()) {
      add(entry.getKey(), entry.getValue());
    }
  }
  
  public void put(State2 e, double p) {
    states.put(e, p);
  }
  
  /** Adds all the Expands to this one with weight p */
  void add(Expands2 expands, double p) {
    for (State2 e: expands.states.keySet()) {
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
  
  public Expander2 getExpander() {
    return this.expander;
  }
  
    /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands2 insert(Item item, boolean missing) throws InterruptedException {
    
    // if (item.getTag().equals(26)) {
    //  System.out.println("============================================================================");
    //  Logger.info("Item %s, missing %s, %d threads", item, missing, expander.threads);
    // }
    
    Queue<Entry<State2, Double>> q = new ArrayDeque<>(states.entrySet());
    List<Worker2> workers = new ArrayList<Worker2>();
    for (int i = 0; i < expander.threads; i++) {
      Worker2 worker = new Worker2(q, this, item, missing);
      worker.start();
      workers.add(worker);
    }
    
    for (Worker2 worker: workers) {
      worker.join();
      //if (item.getTag().equals(26)) System.out.println(worker);
    }
    
    return expands;
  }
  
  private Expands2 expands = null;
  
  synchronized void workerEnd(Worker2 worker) {
    if (expands == null) { expands = worker.getExpands(); }
      else { expands.add(worker.getExpands()); }
  }
  
}
