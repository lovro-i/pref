package edu.drexel.cs.db.rank.gm;

import java.util.ArrayList;
import java.util.List;


public abstract class Variable {

  protected final GraphicalModel gm;
  protected final List<Variable> parents = new ArrayList<Variable>();
  protected final List<Row> rows = new ArrayList<Row>();
  
  public Variable(GraphicalModel gm) {
    this.gm = gm;
  }
  
  
  public abstract String getName();
  
  public void addParent(Variable parent) {
    this.parents.add(parent);
  }
  
  public List<Variable> getParents() {
    return parents;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getName()).append(": ");
    for (Variable v: parents) sb.append(v.getName()).append('\t');
    sb.append("\n");
    for (Row t: rows) sb.append(t).append('\n');
    return sb.toString();
  }
  
  
  protected class Row {
    
    protected final List<Integer> vals = new ArrayList<Integer>();
    protected final int value;
    protected final double p;
    
    protected Row(int value, double p, Integer... vals) {
      this.value = value;
      this.p = p;
      for (Integer i: vals) this.vals.add(i);
    }
    
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Integer i: vals) sb.append(i).append('\t');
      sb.append(p);
      return sb.toString();
    }
    
  }
}
