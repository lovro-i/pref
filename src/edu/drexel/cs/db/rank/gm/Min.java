package edu.drexel.cs.db.rank.gm;


public class Min extends Variable {

  public Min(GraphicalModel gm) {
    super(gm);
  }

  @Override
  public String getName() {
    return "Min";
  }

  @Override
  public void calcFactors() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
