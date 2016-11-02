package edu.drexel.cs.db.db4pref.posterior.concurrent;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.Map;
import java.util.Queue;


public class Worker2 extends Thread {

  private static int nextId = 1;
  private int id = nextId++;

  private final Workers workers;
  
  private Queue<Map.Entry<State2, Double>> queue;
  private Item item;
  private boolean missing;
  private Expands2 dstExpands;
  
  private int count = 0;
  private boolean done = false;
  
  Worker2(Workers workers) {
    this.workers = workers;
  }
  
  public synchronized void run(Queue<Map.Entry<State2, Double>> queue, Expands2 exs, Item item, boolean missing) {
    this.queue = queue;
    this.dstExpands = exs;
    this.item = item;
    this.missing = missing;
    notify();
  }
  
  @Override
  public void run() {
    while (!done) {
      synchronized (this) {
        while (queue == null && !done) {
          try { wait(); }
          catch (InterruptedException e) {}
        }
      }
      
      while (!done) {
        Map.Entry<State2, Double> entry;
        synchronized (queue) {
          entry = queue.poll();
        }
        if (entry == null) break;

        State2 state = entry.getKey();
        double p = entry.getValue();
        state.insert(dstExpands, item, missing, p);
        count++;
      }
      
      this.queue = null;
      workers.onWorkerDone(this);
    }
  }
  
  public Expands2 getExpands() {
    return dstExpands;
  }
  
  public synchronized void done() {
    this.done = true;
    notify();
  }
  
  @Override
  public String toString() {
    return String.format("Worker %d did %d states", id, count);
  }
  
}
