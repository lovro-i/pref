package edu.drexel.cs.db.db4pref.posterior.concurrent4;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.posterior.concurrent4.Workers4.StateProb;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Worker4 extends Thread {

  private final int id;

  private final Workers4 workers;
  
  private ConcurrentLinkedQueue<StateProb> entries;
  private Item item;
  private boolean missing;
  private Expands4 dstExpands;
  
  private int count = 0;
  private boolean done = false;
  
  
  Worker4(Workers4 workers, int id) {
    this.workers = workers;
    this.id = id;
  }
  
  public synchronized void run(ConcurrentLinkedQueue<StateProb> entries, Expands4 exs, Item item, boolean missing) {
    this.entries = entries;
    this.dstExpands = exs;
    this.item = item;
    this.missing = missing;
    notify();
  }
  
  public static long waiting = 0;
  public static long working = 0;
  
  private synchronized void waitEntries() {
    while (entries == null && !done) {
      try { wait(); }
      catch (InterruptedException e) {}
    }
  }
  
  @Override
  public void run() {
    while (true) {
      long t1 = System.currentTimeMillis();
      waitEntries();
      if (done) break;
      waiting += System.currentTimeMillis() - t1;
      
      long t2 = System.currentTimeMillis();
      for (StateProb entry: entries) {
        State4 state = entry.getKey();
        double p = entry.getValue();
        state.insert(dstExpands, item, missing, p);
        count++;
      }
      working += System.currentTimeMillis() - t2;
      
      this.entries = null;
      workers.onWorkerDone(this);
    }
  }
  
  public Expands4 getExpands() {
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
