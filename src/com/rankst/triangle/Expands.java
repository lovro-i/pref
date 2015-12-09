package com.rankst.triangle;

import com.rankst.entity.Element;
import java.util.HashMap;

public class Expands extends HashMap<Expand, Double> {
  
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
  
  /** Adds element e to the right of the element 'prev' in all the Expands.
   *  If (after == null), it is added at the beginning
   */  
  public Expands insert(Element e, Element prev) {
    Expands expands = new Expands();
    for (Expand ex: this.keySet()) {
      double p = this.get(ex);
      Expands exs = ex.insert(e, prev);
      expands.add(exs, p);
    }
    expands.normalize();
    return expands;
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
    return expands;
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
  
  /** Get the sum of weights where element e is at the position pos (zero based) */
  public double count(Element e, int pos) {
    double sum = 0;
    for (int i = 0; i < 10; i++) {
      for (Expand ex: this.keySet()) {
        if (ex.isAt(e, pos)) sum += this.get(ex);
      }
    }
    return sum;
  }
  
  public double[] getDistribution(Element e) {
    double[] dist = null;
    double sum = 0;
    for (Expand ex: this.keySet()) {
      double p = this.get(ex);
      if (dist == null) dist = new double[ex.length()];
      int pos = ex.position(e);
      dist[pos] += p;
      sum += p;
    }    
    for (int i = 0; i < dist.length; i++) {
      dist[i] = dist[i] / sum;
    }
    return dist;
  }
  
}
