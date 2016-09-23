package edu.drexel.cs.db.db4pref.posterior.parallel;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.Map;
import java.util.Queue;


public class Worker2 extends Thread {

  private final Queue<Map.Entry<State2, Double>> queue;
  private final Item item;
  private final boolean missing;
  
  private final Expands2 srcExpands;
  private final Expands2 dstExpands;
  
  private static int nextId = 1;
  private int id = nextId++;
  private long time = 0;
  private int count = 0;
  
  Worker2(Queue<Map.Entry<State2, Double>> q, Expands2 exs, Item item, boolean missing) {
    this.queue = q;
    this.srcExpands = exs;
    this.dstExpands = new Expands2(exs.getExpander());
    this.item = item;
    this.missing = missing;
  }
  
  @Override
  public void run() {
    while (true) {
      Map.Entry<State2, Double> entry;
      synchronized (queue) {
        entry = queue.poll();
      }
      if (entry == null) break;
      long start = System.currentTimeMillis();
      State2 state = entry.getKey();
      double p = entry.getValue();
      state.insert(dstExpands, item, missing, p);
      time += System.currentTimeMillis() - start;
      count++;
    }
    srcExpands.workerEnd(this);
  }
  
  public Expands2 getExpands() {
    return dstExpands;
  }
  
  @Override
  public String toString() {
    return String.format("Worker %d did %d states in %d ms", id, count, time);
  }
  
}
