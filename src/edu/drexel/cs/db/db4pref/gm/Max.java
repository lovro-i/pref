package edu.drexel.cs.db.db4pref.gm;

/** Represents max value of its two parents */
public class Max extends Variable {

  private static int nextId = 1;
  private int id;
  
  public Max(GraphicalModel gm) {
    super(gm);
    id = nextId++;
  }

  @Override
  public String getId() {
    return "Max_" + id;
  }
  
  @Override
  public String getName() {
    return "Max";
  }

  @Override
  protected void calcFactors() {
    if (parents.size() != 2) throw new IllegalStateException("MAX must have two parents");
    rows.clear();
    for (Integer i: parents.get(0).getValues()) {
      for (Integer j: parents.get(1).getValues()) {
        if (i != j) this.addRow(Math.max(i, j), 1, i, j);
      }
    }
  }

}
