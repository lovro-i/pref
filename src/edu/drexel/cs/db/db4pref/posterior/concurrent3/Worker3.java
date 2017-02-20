package edu.drexel.cs.db.db4pref.posterior.concurrent3;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;


public class Worker3 extends Thread {

  private static int nextId = 1;
  final int id = nextId++;

  private final Workers3 workers;
  
  final Queue<Entry<State3, Double>> queue = new LinkedList<>();
  private Item item;
  private boolean missing;
  private Expands3 dstExpands;
  
  private int count = 0;
  private boolean done = false;
  private boolean signal = false;
  private boolean signaled = false;
  
  Worker3(Workers3 workers) {
    this.workers = workers;
  }
  
  public synchronized void init(Expands3 exs, Item item, boolean missing) {
    if (!queue.isEmpty()) Logger.warn("Worker %d queue is not empty: %d", id, queue.size());
    this.dstExpands = exs;
    this.item = item;
    this.missing = missing;
    this.signal = signaled = false;
  }

  public synchronized void add(Entry<State3, Double> entry) {
    queue.add(entry);
    notify();
  }
  
  public synchronized void setSignal() {
    this.signal = true;
    notify();
  }
  
  public synchronized Entry<State3, Double> getNext() {
    while (true) {
      if (done) return null;
      Entry<State3, Double> entry = queue.poll();
      if (entry != null || signal) return entry;
      try { wait(); }
      catch (InterruptedException e) { }
    }
  }
  
  public static long waiting = 0;
  public static long working = 0;
  
  @Override
  public void run() {
    Entry<State3, Double> entry;
    while (true) {
      long t1 = System.currentTimeMillis();
      entry = getNext();
      waiting += System.currentTimeMillis() - t1;
      
      if (done) break;
      
      if (entry != null) {
        long t2 = System.currentTimeMillis();
        State3 state = entry.getKey();
        double p = entry.getValue();
        state.insert(dstExpands, item, missing, p);
        count++;
        working += System.currentTimeMillis() - t2;
      }
      
      synchronized (this) {
        if (signal && !signaled && queue.isEmpty()) {
          workers.onWorkerDone(this);
          signaled = true;
        }
      }
    }
  }
  
  public Expands3 getExpands() {
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
