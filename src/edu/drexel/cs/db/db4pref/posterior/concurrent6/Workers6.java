package edu.drexel.cs.db.db4pref.posterior.concurrent6;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Workers6 {

  final int threads;
  
  Workers6(int threads) {
    this.threads = threads;
  }
  
  public synchronized void run(List<ConcurrentHashMap<State6, Double>> statess, Expands6 expands, Item item, boolean missing) {
    Worker6 worker = new Worker6(expands, item, missing);
    for (ConcurrentHashMap<State6, Double> states: statess) {
      if (threads == 1) states.forEach(worker);
      else states.forEach(threads, worker);
    }
  }
  
}
