package edu.drexel.cs.db.db4pref.posterior.concurrent;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class Workers {

  private final List<Worker2> workers = new ArrayList<Worker2>();
  private int pending;
  
  Workers(int threads) {
    for (int i = 0; i < threads; i++) {
      Worker2 worker = new Worker2(this);
      worker.start();
      workers.add(worker);
    }
  }
  
  public synchronized void run(Queue<Map.Entry<State2, Double>> queue, Expands2 expands, Item item, boolean missing) {
    this.pending = workers.size();
    
    for (Worker2 worker: workers) {
      worker.run(queue, expands, item, missing);
    }
    
    while (pending > 0) {
      try { wait(); }
      catch (InterruptedException e) {}
    }
  }
  
  synchronized void onWorkerDone(Worker2 worker) {
    this.pending--;
    this.notify();
  }
  
  public void stop() {
    for (Worker2 worker: workers) {
      worker.done();
    }
    for (Worker2 worker: workers) {
      try { worker.join(); }
      catch (InterruptedException ex) {}
    }
  }
}
