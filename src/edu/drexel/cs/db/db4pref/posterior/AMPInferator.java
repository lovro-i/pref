package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.sampler.AMPSampler;


public class AMPInferator {

  private final MallowsModel model;
  private final PreferenceSet v;
  
  private double sum;
  private int count;
  
  public AMPInferator(MallowsModel model, PreferenceSet v) {
    this.model = model;
    this.v = v;
  }
  
  /** Get result of sampling for <i>millis</i> milliseconds 
   * For multiple calls, it aggregates all results
   */
  public double sampleMillis(long millis) {
    long start = System.currentTimeMillis();
    AMPSampler ampSampler = new AMPSampler(model);
    do {
      sum += ampSampler.samplePosterior(v);
      count++;
    }
    while (System.currentTimeMillis() - start < millis);
    return sum / count;
  }
  
  
  /** Get result of sampling for <i>c</i> samples
   * For multiple calls, it aggregates all results
   */
  public double sampleCount(int c) {
    AMPSampler ampSampler = new AMPSampler(model);
    for (int i = 0; i < c; i++) {
      sum += ampSampler.samplePosterior(v);
      count++;
    }
    return sum / count;
  }
  

  
  public double getProbability() {
    return sum / count;
  }
  
  public int getCount() {
    return count;
  }
  
}
