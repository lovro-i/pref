package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/** Mapping from Expand states to their probabilities */
public class DynamicPreferenceExpands extends HashMap<DynamicPreferenceExpand, Double> {
  
  /** The owner of this object */
  private final DynamicPreferenceExpander expander;
  
  public DynamicPreferenceExpands(DynamicPreferenceExpander expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    this.clear();
    this.put(new DynamicPreferenceExpand(expander), 1d);
  }
  
  public void add(DynamicPreferenceExpand e, Double p) {
    Double prev = this.get(e);
    if (prev != null) p += prev;
    this.put(e, p);
  }
  

  public DynamicPreferenceExpands insert(Item item) throws TimeoutException {
    DynamicPreferenceExpands expands = new DynamicPreferenceExpands(expander);
    for (DynamicPreferenceExpand ex: this.keySet()) {
      if (expander.timeout > 0 && System.currentTimeMillis() - expander.start > expander.timeout) throw new TimeoutException("Expander timeout exceeded");
      double p = this.get(ex);
      DynamicPreferenceExpands exs = ex.insert(item);
      expands.add(exs, p);
    }
    return expands;
  }
  

  
  /** Normalizes sum of p to 1 */
  public void normalize() {
    double sum = 0;
    for (Double p: this.values()) {
      sum += p;
    }
    for (DynamicPreferenceExpand e: this.keySet()) {
      Double v = this.get(e);
      this.put(e, v / sum);
    }    
  }
  
  /** Adds all the Expands to this one with weight p */
  public void add(DynamicPreferenceExpands expands, double p) {
    for (DynamicPreferenceExpand e: expands.keySet()) {
      double v = expands.get(e);
      this.add(e, p * v);
    }
  }
  
  
  /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public DynamicPreferenceExpands insertMissing(Item item) throws TimeoutException {
    DynamicPreferenceExpands expands = new DynamicPreferenceExpands(expander);    
    for (DynamicPreferenceExpand ex: this.keySet()) {
      if (expander.timeout > 0 && System.currentTimeMillis() - expander.start > expander.timeout) throw new TimeoutException("Expander timeout exceeded");
      DynamicPreferenceExpands exs = ex.insertMissing(item);
      expands.add(exs, this.get(ex));
    }
    //expands.normalize();
    return expands;
  }


  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (DynamicPreferenceExpand expand: this.keySet()) {
      sb.append(expand).append(": ").append(this.get(expand)).append("\n");
    }
    return sb.toString();
  }
  
  
  /** Get the sum of weights where item e is at the position pos (zero based) */
  public double count(Item e, int pos) {
    double sum = 0;
    for (int i = 0; i < 10; i++) {
      for (DynamicPreferenceExpand ex: this.keySet()) {
        if (ex.isAt(e, pos)) sum += this.get(ex);
      }
    }
    return sum;
  }
  
  /** Distribution of item e being at different positions */
  public double[] getDistribution(Item e) {
    double[] dist = null;
    double sum = 0;
    for (DynamicPreferenceExpand ex: this.keySet()) {
      double p = this.get(ex);
      if (dist == null) dist = new double[ex.length()];
      int pos = ex.position(e);
      dist[pos] += p;
      sum += p;
    }
    
    if (dist != null && sum > 0) {
      for (int i = 0; i < dist.length; i++) {
        dist[i] = dist[i] / sum;
      }
    }
    else {
      dist = new double[0];
    }
    return dist;
  }

  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (double p: this.values()) sum += p;
    return sum;
  }
  
}
