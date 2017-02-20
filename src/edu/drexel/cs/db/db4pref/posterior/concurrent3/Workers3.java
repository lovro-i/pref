package edu.drexel.cs.db.db4pref.posterior.concurrent3;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class Workers3 {

  private final List<Worker3> workers = new ArrayList<Worker3>();
  private int pending;
  private final int threads;
  
  Workers3(int threads) {
    this.threads = threads;
    for (int i = 0; i < threads; i++) {
      Worker3 worker = new Worker3(this);
      worker.start();
      workers.add(worker);
    }
  }
  
  
  public synchronized void run(Set<Map.Entry<State3, Double>> entrySet, Expands3 expands, Item item, boolean missing) {
    this.pending = workers.size();

    for (Worker3 worker: workers) {
      worker.init(expands, item, missing);
    }
    
    int idx = 0;
    for (Entry<State3, Double> entry: entrySet) {
      Worker3 worker = workers.get(idx);
      worker.add(entry);
      idx = (idx + 1) % threads;
    }
    
    for (Worker3 worker: workers) {
      worker.setSignal();
    }
    
    while (pending > 0) {
      try { wait(); }
      catch (InterruptedException e) {}
    }
    
  }
  
  synchronized void onWorkerDone(Worker3 worker) {
    this.pending--;
    this.notify();
  }
  
  public void stop() {
    for (Worker3 worker: workers) {
      worker.done();
    }
    for (Worker3 worker: workers) {
      try { worker.join(); }
      catch (InterruptedException ex) {}
    }
  }
}
