package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;


public class Xii extends Xij {

  
  public Xii(GraphicalModel gm, int i) {
    super(gm, i, i);
    this.type = Type.RIM;
  }
  
  
  public Xii(GraphicalModel gm, Item item) {
    this(gm, gm.getModel().getCenter().indexOf(item));
  }
  
  @Override
  public void calcFactors() {
    rows.clear();
    if (type == Type.AFTER) {
      if (this.parents.size() != 1) throw new IllegalStateException("AFTER must have one parent: " + this);
      Variable var = this.parents.get(0);
      for (Integer v: var.getValues()) {
        for (int i = v+1; i <= index; i++) {
          addRow(i, probability(i), v);
        }
      }
    }
    else if (type == Type.BEFORE) {
      if (this.parents.size() != 1) throw new IllegalStateException("BEFORE must have one parent: " + this);
      Variable var = this.parents.get(0);
      for (Integer v: var.getValues()) {
        for (int i = 0; i <= v; i++) {
          addRow(i, probability(i), v);
        }
      }
    }
    else if (type == Type.INS) {
      if (this.parents.size() != 2) throw new IllegalStateException("INS must have two parents: " + this);
      Variable after = this.parents.get(0);
      Variable before = this.parents.get(1);
      for (Integer a: after.getValues()) {
        for (Integer b: before.getValues()) {
          for (int i = a+1; i <= b; i++) {
            addRow(i, probability(i), a, b);
          }
        }
      }
    }
    else if (type == Type.RIM) {
      if (!this.parents.isEmpty()) throw new IllegalStateException("RIM must have zero parents: " + this);
      for (int i = 0; i <= index; i++) {
        addRow(i, probability(i));
      }
    }
    else throw new IllegalStateException(String.format("Illegal type of variable %s: %s", this, type));
  }
  
  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  private double probability(int position) {
    if (index == 0) return 1d;
    double phi = gm.getModel().getPhi();
    double p = Math.pow(phi, index - position) * (1 - phi) / (1 - Math.pow(phi, index + 1));
    return p;
  }
  

  
}
