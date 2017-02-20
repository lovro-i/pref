package edu.drexel.cs.db.db4pref.posterior.concurrent5;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.concurrent.ConcurrentHashMap;


public class Workers5 {

  final int threads;
  
  Workers5(int threads) {
    this.threads = threads;
  }
  
  public synchronized void run(ConcurrentHashMap<State5, Double> states, Expands5 expands, Item item, boolean missing) {
    Worker5 worker = new Worker5(expands, item, missing);
    states.forEach(threads, worker);
  }
  
}
