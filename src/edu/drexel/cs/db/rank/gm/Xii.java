package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;


public class Xii extends Xij {

  
  public Xii(GraphicalModel gm, int i) {
    super(gm, i, i);
  }
  
  
  public Xii(GraphicalModel gm, Item item) {
    this(gm, gm.getModel().getCenter().indexOf(item));
  }
  
  @Override
  public void calcFactors() {
    for (int i = 0; i <= index; i++) {
      double p = probability(i);
      Row touple = new Row(i, p);
      this.rows.add(touple);
    }
  }
  
  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  private double probability(int position) {
    double phi = gm.getModel().getPhi();
    double r = Math.pow(phi, Math.abs(index - position));
    return r;
  }
  

  
}
