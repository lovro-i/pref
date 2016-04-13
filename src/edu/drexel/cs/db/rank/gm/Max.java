package edu.drexel.cs.db.rank.gm;


public class Max extends Variable {

  public Max(GraphicalModel gm) {
    super(gm);
  }

  @Override
  public String getName() {
    return "Max";
  }

  @Override
  public void calcFactors() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
