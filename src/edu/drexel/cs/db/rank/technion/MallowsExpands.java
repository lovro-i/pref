package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.HashMap;

public class MallowsExpands extends HashMap<MallowsExpand, Double> {
  
  private Expander expander;
  
  public MallowsExpands(Expander expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    this.clear();
    this.put(new MallowsExpand(expander), 1d);
  }
  
  public void add(MallowsExpand e, Double p) {
    Double prev = this.get(e);
    if (prev != null) p += prev;
    this.put(e, p);
  }
  
  /** Adds element e to the right of the element 'prev' in all the Expands.
   *  If (after == null), it is added at the beginning
   */  
  public MallowsExpands insert(Element e, Element prev) {
    MallowsExpands expands = new MallowsExpands(expander);
    for (MallowsExpand ex: this.keySet()) {
      double p = this.get(ex);
      MallowsExpands exs = ex.insert(e, prev);
      expands.add(exs, p);
    }
    //expands.normalize();
    return expands;
  }
  

  
  /** Normalizes sum of p to 1 */
  public void normalize() {
    double sum = 0;
    for (Double p: this.values()) {
      sum += p;
    }
    for (MallowsExpand e: this.keySet()) {
      Double v = this.get(e);
      this.put(e, v / sum);
    }    
  }
  
  /** Adds all the Expands to this one with weight p */
  public void add(MallowsExpands expands, double p) {
    for (MallowsExpand e: expands.keySet()) {
      double v = expands.get(e);
      this.add(e, p * v);
    }
  }
  
  public MallowsExpands insertMissing(Element element) {
    MallowsExpands expands = new MallowsExpands(expander);    
    for (MallowsExpand ex: this.keySet()) {
      MallowsExpands exs = ex.insertMissing(element);
      expands.add(exs, this.get(ex));
    }
    //expands.normalize();
    return expands;
  }


  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (MallowsExpand expand: this.keySet()) {
      sb.append(expand).append(": ").append(this.get(expand)).append("\n");
    }
    return sb.toString();
  }
  
  /** Get the sum of weights where element e is at the position pos (zero based) */
  public double count(Element e, int pos) {
    double sum = 0;
    for (int i = 0; i < 10; i++) {
      for (MallowsExpand ex: this.keySet()) {
        if (ex.isAt(e, pos)) sum += this.get(ex);
      }
    }
    return sum;
  }
  
  /** Distribution of element e being at different positions */
  public double[] getDistribution(Element e) {
    double[] dist = null;
    double sum = 0;
    for (MallowsExpand ex: this.keySet()) {
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
