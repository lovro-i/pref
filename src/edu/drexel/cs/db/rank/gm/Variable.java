package edu.drexel.cs.db.rank.gm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
    return getName();
  }
  
  /** Return set of values that this variable can have */
  public Set<Integer> getValues() {
    if (rows.isEmpty()) calcFactors();
    Set<Integer> vals = new HashSet<Integer>();
    for (Row row: rows) {
      vals.add(row.value);
    }
    return vals;
  }
  
  public abstract void calcFactors();
  
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
