package edu.drexel.cs.db.rank.top;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.triangle.TriangleRow;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TopExpands extends HashMap<TopExpand, Double> {
  
  private static double threshold = 0.0001;
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    this.clear();
    this.put(new TopExpand(), 1d);
  }
  
  public void add(TopExpand e, Double p) {
    Double prev = this.get(e);
    if (prev != null) p += prev;
    this.put(e, p);
  }
  
  /** Adds item e to the right of the item 'prev' in all the Expands.
   *  If (after == null), it is added at the beginning
   */  
  public TopExpands insert(Item e, Item prev) {
    TopExpands expands = new TopExpands();
    for (TopExpand ex: this.keySet()) {
      double p = this.get(ex);
      TopExpands exs = ex.insert(e, prev);
      expands.add(exs, p);
    }
    expands.normalize();
    expands.prune();
    return expands;
  }
  
  public static void setThreshold(double value) {
    TopExpands.threshold = value;
  }
  
  public static double getThreshold() {
    return threshold;
  }
  
  
  /** Normalizes sum of p to 1 */
  public void normalize() {
    double sum = 0;
    for (Double p: this.values()) {
      sum += p;
    }
    for (TopExpand e: this.keySet()) {
      Double v = this.get(e);
      this.put(e, v / sum);
    }    
  }
  
  /** Adds all the Expands to this one with weight p */
  public void add(TopExpands expands, double p) {
    for (TopExpand e: expands.keySet()) {
      double v = expands.get(e);
      this.add(e, p * v);
    }
  }
  
  public TopExpands insertMissing() {
    TopExpands expands = new TopExpands();
    for (TopExpand e: this.keySet()) {
      TopExpands exs = e.insertMissing();
      expands.add(exs, this.get(e));
    }
    expands.normalize();
    expands.prune();
    return expands;
  }
  
  private void prune() {
    if (threshold <= 0) return;    
    Iterator<Map.Entry<TopExpand, Double>> it = this.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<TopExpand, Double> entry = it.next();
      if (entry.getValue() < threshold) it.remove();
    }   
  } 
  
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (TopExpand expand: this.keySet()) {
      sb.append(expand).append(": ").append(this.get(expand)).append("\n");
    }
    return sb.toString();
  }
  
  /** Get the sum of weights where item e is at the position pos (zero based) */
  public double count(Item e, int pos) {
    double sum = 0;
    for (int i = 0; i < 10; i++) {
      for (TopExpand ex: this.keySet()) {
        if (ex.isAt(e, pos)) sum += this.get(ex);
      }
    }
    return sum;
  }
  
  /** Distribution of item e being at different positions */
  public double[] getDistribution(Item e) {
    double[] dist = null;
    double sum = 0;
    for (TopExpand ex: this.keySet()) {
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
