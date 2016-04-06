package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;
import java.util.Set;


public class Xij extends Variable {

  protected final Item item;
  protected final int index;
  protected final int t;
  
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
    return "X" + item + "^" + (t + 1);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Xij)) return false;
    Xij xij = (Xij) o;
    return xij.getIndex() == this.getIndex() && xij.getTime() == this.getTime() && xij.gm.equals(gm);
  }
}
