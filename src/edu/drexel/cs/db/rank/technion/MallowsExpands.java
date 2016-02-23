package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.core.Item;
import java.util.HashMap;

/** Mapping from Expand states to their probabilities */
public class MallowsExpands extends HashMap<MallowsExpand, Double> {
  
  /** The owner of this object */
  private final Expander expander;
  
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
  
  /** Adds item e to the right of the item 'prev' in all the Expands.
   *  If <code>prev</code> is null, it is added at the beginning
   * @return Map of union of the states and their probabilities expanded after adding item e after prev to all expand states
   */  
  public MallowsExpands insert(Item e, Item prev) {
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
  
  
  /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public MallowsExpands insertMissing(Item item) {
    MallowsExpands expands = new MallowsExpands(expander);    
    for (MallowsExpand ex: this.keySet()) {
      MallowsExpands exs = ex.insertMissing(item);
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
  
  
  /** Get the sum of weights where item e is at the position pos (zero based) */
  public double count(Item e, int pos) {
    double sum = 0;
    for (int i = 0; i < 10; i++) {
      for (MallowsExpand ex: this.keySet()) {
        if (ex.isAt(e, pos)) sum += this.get(ex);
      }
    }
    return sum;
  }
  
  /** Distribution of item e being at different positions */
  public double[] getDistribution(Item e) {
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

  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (double p: this.values()) sum += p;
    return sum;
  }
  
}
