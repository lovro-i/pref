package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.sampler.AMPSampler;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/** Approximate posterior probability by using AMP and importance sampling
 */
public class ParallelAMPInferator {

  private final MallowsModel model;
  private final PreferenceSet v;
  
  private double sum = 0;
  private int count = 0;
  private final int threads;
  
  public ParallelAMPInferator(MallowsModel model, PreferenceSet v, int threads) {
    this.model = model;
    this.v = v;
    this.threads = threads;
  }
  
  public ParallelAMPInferator(MallowsModel model, PreferenceSet v) {
    this(model, v, Runtime.getRuntime().availableProcessors());
  }
  
  /** Get result of sampling for <i>millis</i> milliseconds 
   * For multiple calls, it aggregates all results
   */
  public double sampleMillis(long millis) {
    List<Sampler> samplers = new ArrayList<Sampler>();
    for (int i = 0; i < threads; i++) {
      Sampler sampler = new Sampler(millis);
      sampler.start();
      samplers.add(sampler);
    }

    for (Sampler sampler: samplers) {
      try { 
        sampler.join(); 
        this.sum += sampler.sum;
        this.count += sampler.count;
      }
      catch (InterruptedException e) {}
    }
    return getProbability();
  }
  
  
  public double getProbability() {
    return sum / count;
  }
  
  public int getCount() {
    return count;
  }
 
  
  private class Sampler extends Thread {
    
    private AMPSampler ampSampler = new AMPSampler(model);
    private long millis;
    
    double sum = 0;
    int count = 0;
    
    Sampler(long millis) {
      this.millis = millis;
    }
    
    @Override
    public void run() {
      long start = System.currentTimeMillis();
      while (true) {
        this.sum += ampSampler.samplePosterior(v);
        this.count++;
        if (System.currentTimeMillis() - start >= millis) return;
      }
    }
    
  }
}
