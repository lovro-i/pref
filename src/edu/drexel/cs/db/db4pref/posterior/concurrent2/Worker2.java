package edu.drexel.cs.db.db4pref.posterior.concurrent2;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.DoubleAdder;


public class Worker2 extends Thread {

  private static int nextId = 1;
  private int id = nextId++;

  private final Workers2 workers;
  
  private Queue<Map.Entry<State2, DoubleAdder>> queue;
  private Item item;
  private boolean missing;
  private boolean isLowerBound;
  private Expands2 dstExpands;
  
  private int count = 0;
  private boolean done = false;
  
  Worker2(Workers2 workers) {
    this.workers = workers;
  }
  
  public synchronized void run(Queue<Map.Entry<State2, DoubleAdder>> queue, Expands2 exs, Item item, boolean missing, boolean isLowerBound) {
    this.queue = queue;
    this.dstExpands = exs;
    this.item = item;
    this.missing = missing;
    this.isLowerBound = isLowerBound;
    notify();
  }
  
  public static long waiting = 0;
  public static long working = 0;
  
  @Override
  public void run() {
    while (!done) {
      long t1 = System.currentTimeMillis();
      synchronized (this) {
        while (queue == null && !done) {
          try { wait(); }
          catch (InterruptedException e) {}
        }
      }
      waiting += System.currentTimeMillis() - t1;
      
      long t2 = System.currentTimeMillis();
      while (!done) {
        Map.Entry<State2, DoubleAdder> entry = queue.poll();
        if (entry == null) break;

        State2 state = entry.getKey();
        double p = entry.getValue().doubleValue();
        state.insert(dstExpands, item, missing, isLowerBound, p);
        count++;
      }
      working += System.currentTimeMillis() - t2;
      
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
