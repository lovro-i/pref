package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.Objects;
import java.util.Set;


public class Xij extends Variable {

  protected final Item item;
  protected final int index; // Index of the item
  protected final int t;
  
  static enum Type { POS1, POS2, AFTER, BEFORE, INS, RIM } 
  protected Type type;
  
  
  public Xij(GraphicalModel gm, Item item, int t) {
    super(gm);
    this.item = item;
    this.index = gm.getModel().getCenter().indexOf(item);
    this.t = t;
  }
  
  public Xij(GraphicalModel gm, int i, int t) {
    super(gm);
    this.item = gm.getModel().getCenter().get(i);
    this.index = i;
    this.t = t;
  }
  
  
  public Item getItem() {
    return item;
  }
  
  public int getIndex() {
    return index;
  }
  
  public int getTime() {
    return t;
  }
  
  public Set<Variable> getChildren() {
    return gm.getChildren(this);
  }
  
  @Override
  public String getName() {
    return "X" + item + "^" + (t + gm.getBase());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Xij)) return false;
    Xij xij = (Xij) o;
    return xij.getIndex() == this.getIndex() && xij.getTime() == this.getTime() && xij.gm.equals(gm);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + Objects.hashCode(this.item);
    hash = 59 * hash + this.t;
    return hash;
  }

  void setType(Type type, Variable... vars) {
    this.type = type;
    this.parents.clear();
    for (Variable var: vars) {
      this.parents.add(var);
    }
  }
  
  void setPos1(Xij parent) {
    if (!this.item.equals(parent.getItem())) throw new IllegalStateException("POS1 parent must be of the same item: " + this);
    setType(Type.POS1, parent);
  }
  
  void setPos2(Xij prev, Xii ins) {
    if (!this.item.equals(prev.getItem())) throw new IllegalStateException("POS1 parent must be of the same item: " + this);
    setType(Type.POS2, prev, ins);
  }
  
  void setAfter(Variable var) {
    setType(Type.AFTER, var);
  }
  
  void setBefore(Variable var) {
    setType(Type.BEFORE, var);
  }

  void setIns(Variable after, Variable before) {
    setType(Type.INS, after, before);
  }
  
  @Override
  public void calcFactors() {
    rows.clear();
    if (type == Type.POS2) {
      if (this.parents.size() != 2) throw new IllegalStateException("POS2 must have two parents: " + this);
      Xij prev = (Xij) parents.get(0);
      Xii ins = (Xii) parents.get(1);
      for (Integer pr: prev.getValues()) {
        for (Integer in: ins.getValues()) {
          if (in <= pr) this.addRow(pr+1, 1, pr, in);
          else this.addRow(pr, 1, pr, in);
        }
      }
    }
    else if (type == Type.POS1) {
      if (this.parents.size() != 1) throw new IllegalStateException("POS1 must have one parent: " + this);
      Xij prev = (Xij) parents.get(0);
      if (!this.item.equals(prev.getItem())) throw new IllegalStateException("POS1 parent must be of the same item: " + this);
      for (int k: prev.getValues()) {
        for (int t = k; t <= this.getTime(); t++) {
          double p = gm.pos1.getProbability(item, this.getTime(), t, prev.getTime(), k);
          this.addRow(t, p, k);
        }
      }
    }
    else throw new IllegalStateException(String.format("Illegal type of variable %s: %s", this, type));
  }
}
