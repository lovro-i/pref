package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;

@Deprecated
public class BeforeVariable extends Variable {

  private final int step;
  private final Item item;

  public BeforeVariable(GraphicalModel gm, Item item, int step) {
    super(gm);
    this.item = item;
    this.step = step;
  }
  
  @Override
  public String getName() {
    StringBuilder sb = new StringBuilder();
    sb.append("BeforeVariable ").append(item).append("^").append(step);
    return sb.toString();
  }
}
