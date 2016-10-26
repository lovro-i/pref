package edu.drexel.cs.db.db4pref.posterior.concurrent;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.Map;
import java.util.Queue;


public class Worker2 extends Thread {

  private final Queue<Map.Entry<State2, Double>> queue;
  private final Item item;
  private final boolean missing;
  
  private final Expands2 dstExpands;
  
  private static int nextId = 1;
  private int id = nextId++;
  
  private long wait = 0;
  private long work = 0;
  private int count = 0;
  
  Worker2(Queue<Map.Entry<State2, Double>> queue, Expands2 exs, Item item, boolean missing) {
    this.queue = queue;
    this.dstExpands = exs;
    this.item = item;
    this.missing = missing;
  }
  
  @Override
  public void run() {
    while (true) {
      Map.Entry<State2, Double> entry;
      long s1 = System.currentTimeMillis();
      synchronized (queue) {
        entry = queue.poll();
      }
      this.wait += System.currentTimeMillis() - s1;
      if (entry == null) break;
      long start = System.currentTimeMillis();
      State2 state = entry.getKey();
      double p = entry.getValue();
      state.insert(dstExpands, item, missing, p);
      work += System.currentTimeMillis() - start;
      count++;
    }
  }
  
  public Expands2 getExpands() {
    return dstExpands;
  }
  
  @Override
  public String toString() {
    return String.format("Worker %d did %d states: %d ms wait + %d ms work", id, count, wait, work);
  }
  
}
