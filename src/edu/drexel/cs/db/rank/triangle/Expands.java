package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Expands extends HashMap<Expand, Double> {
  
  private static double threshold = 0.0001;
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    this.clear();
    this.put(new Expand(), 1d);
  }
  
  public void add(Expand e, Double p) {
    Double prev = this.get(e);
    if (prev != null) p += prev;
    this.put(e, p);
  }
  
  /** Adds item e to the right of the item 'prev' in all the Expands.
   *  If (after == null), it is added at the beginning
   */  
  public Expands insert(Item e, Item prev) {
    Expands expands = new Expands();
    for (Expand ex: this.keySet()) {
      double p = this.get(ex);
      Expands exs = ex.insert(e, prev);
      expands.add(exs, p);
    }
    expands.normalize();
    expands.prune();
    return expands;
  }
  
  public static void setThreshold(double value) {
    Expands.threshold = value;
  }
  
  
  /** Normalizes sum of p to 1 */
  public void normalize() {
    double sum = 0;
    for (Double p: this.values()) {
      sum += p;
    }
    for (Expand e: this.keySet()) {
      Double v = this.get(e);
      this.put(e, v / sum);
    }    
  }
  
  /** Adds all the Expands to this one with weight p */
  public void add(Expands expands, double p) {
    for (Expand e: expands.keySet()) {
      double v = expands.get(e);
      this.add(e, p * v);
    }
  }
  
  public Expands insertMissing() {
    Expands expands = new Expands();
    for (Expand e: this.keySet()) {
      Expands exs = e.insertMissing();
      expands.add(exs, this.get(e));
    }
    expands.normalize();
    expands.prune();
    return expands;
  }
  
  private void prune() {
    if (threshold <= 0) return;    
    Iterator<Map.Entry<Expand, Double>> it = this.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Expand, Double> entry = it.next();
      if (entry.getValue() < threshold) it.remove();
    }   
  } 
  
  public Expands insertMissing(TriangleRow row) {
    Expands expands = new Expands();
    for (Expand e: this.keySet()) {
      Expands exs = e.insertMissing(row);
      expands.add(exs, this.get(e));
    }
    expands.normalize();
    return expands;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Expand expand: this.keySet()) {
      sb.append(expand).append(": ").append(this.get(expand)).append("\n");
    }
    return sb.toString();
  }
  
  /** Get the sum of weights where item e is at the position pos (zero based) */
  public double count(Item e, int pos) {
    double sum = 0;
    for (int i = 0; i < 10; i++) {
      for (Expand ex: this.keySet()) {
        if (ex.isAt(e, pos)) sum += this.get(ex);
      }
    }
    return sum;
  }
  
  /** Distribution of item e being at different positions */
  public double[] getDistribution(Item e) {
    double[] dist = null;
    double sum = 0;
    for (Expand ex: this.keySet()) {
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
  
}
