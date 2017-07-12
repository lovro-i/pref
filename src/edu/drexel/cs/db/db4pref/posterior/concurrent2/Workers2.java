package edu.drexel.cs.db.db4pref.posterior.concurrent2;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;


public class Workers2 {

  private final List<Worker2> workers = new ArrayList<Worker2>();
  private int pending;
  
  Workers2(int threads) {
    for (int i = 0; i < threads; i++) {
      Worker2 worker = new Worker2(this);
      worker.start();
      workers.add(worker);
    }
  }
  
  
  public synchronized void run(Set<Map.Entry<State2, DoubleAdder>> entrySet, Expands2 expands, Item item, boolean missing) {
    this.pending = workers.size();


    int block = entrySet.size() / workers.size();
    Iterator<Entry<State2, DoubleAdder>> it = entrySet.iterator();
    for (int i = 0; i < workers.size(); i++) {
      Queue<Entry<State2, DoubleAdder>> queue = new LinkedList<>();
      for (int j = 0; j < block + workers.size() - i && it.hasNext(); j++) {
        queue.add(it.next());
      }
      if (i == workers.size() - 1) {
        while (it.hasNext()) queue.add(it.next());
      }
      
      
      Worker2 worker = workers.get(i);
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
