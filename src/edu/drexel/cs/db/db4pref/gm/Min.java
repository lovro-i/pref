package edu.drexel.cs.db.db4pref.gm;


public class Min extends Variable {

  private static int nextId = 1;
  private int id;
  
  public Min(GraphicalModel gm) {
    super(gm);
    this.id = nextId++;
  }
  
  @Override
  public String getId() {
    return "Min_" + id;
  }
  
  @Override
  public String getName() {
    return "Min";
  }

  @Override
  public void calcFactors() {
    if (parents.size() != 2) throw new IllegalStateException("MIN must have two parents");
    rows.clear();
    for (Integer i: parents.get(0).getValues()) {
      for (Integer j: parents.get(1).getValues()) {
        if (i != j) this.addRow(Math.min(i, j), 1, i, j);
      }
    }
  }

}
