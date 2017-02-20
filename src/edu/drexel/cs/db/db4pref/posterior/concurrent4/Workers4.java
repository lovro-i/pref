package edu.drexel.cs.db.db4pref.posterior.concurrent4;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Workers4 {

  private final List<Worker4> workers = new ArrayList<Worker4>();
  private int pending;
  final int threads;
  
  Workers4(int threads) {
    this.threads = threads;
    for (int i = 0; i < threads; i++) {
      Worker4 worker = new Worker4(this, i);
      worker.start();
      workers.add(worker);
    }
  }
  
  public static long preparing = 0;
  
  static class StateProb implements Entry<State4, Double> {

    private final State4 state;
    private final Double p;

    StateProb(State4 state, Double p) {
      this.state = state;
      this.p = p;
    }
    
    @Override
    public State4 getKey() {
      return state;
    }

    @Override
    public Double getValue() {
      return p;
    }

    @Override
    public Double setValue(Double value) {
      throw new UnsupportedOperationException("Not supported");
    }
    
  }
  
  private class Separator implements java.util.function.BiConsumer<State4, Double> {

    final int threads;
    List<ConcurrentLinkedQueue<StateProb>> lists = new ArrayList<>();
    private Random random = new Random();
    
    Separator(int threads) {
      this.threads = threads;
      for (int i = 0; i < threads; i++) {
        ConcurrentLinkedQueue<StateProb> list = new ConcurrentLinkedQueue<>();
        lists.add(list);
      }
    }
    
    @Override
    public void accept(State4 state, Double p) {
      // int idx = Math.abs(state.hashCode()) % threads;
      int idx = random.nextInt(threads);
      lists.get(idx).add(new StateProb(state, p));
    }
    
  }
  
  
  public synchronized void run(ConcurrentHashMap<State4, Double> states, Expands4 expands, Item item, boolean missing) {
    this.pending = workers.size();

    long t1 = System.currentTimeMillis();
    Separator separator = new Separator(threads);
    states.forEach(threads, separator);
    preparing += System.currentTimeMillis() - t1;
    
    for (int i = 0; i < workers.size(); i++) {
      Worker4 worker = workers.get(i);
      worker.run(separator.lists.get(i), expands, item, missing);
    }
    
    while (pending > 0) {
      try { wait(); }
      catch (InterruptedException e) {}
    }
  }
  
  synchronized void onWorkerDone(Worker4 worker) {
    this.pending--;
    this.notify();
  }
  
  public void stop() {
    for (Worker4 worker: workers) {
      worker.done();
    }
    for (Worker4 worker: workers) {
      try { worker.join(); }
      catch (InterruptedException ex) {}
    }
  }
}
