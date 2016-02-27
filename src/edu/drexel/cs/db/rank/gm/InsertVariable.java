package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;


public class InsertVariable extends Variable {

  private Item item;
  
  public InsertVariable(GraphicalModel gm, Item item) {
    super(gm);
    this.item = item;
    build();
  }
  
  
  public void build() {
    this.vars.add(this);
    for (int i = 0; i <= item.id; i++) {
      double p = probability(i);
      Row touple = new Row(i, p);
      this.rows.add(touple);
    }
  }
  
  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  private double probability(int position) {
    double phi = gm.getModel().getPhi();
    double r = Math.pow(phi, Math.abs(item.id - position));
    return r;
  }
  
  
  @Override
  public String getName() {
    return item + "@" + item.getId();
  }
  
}
