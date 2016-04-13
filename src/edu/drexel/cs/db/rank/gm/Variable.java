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
  
  public String getId() {
    return getName();
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
  
  public void addRow(int value, double p, Integer... vals) {
    Row row = new Row(value, p, vals);
    this.rows.add(row);
  }
  
  /** Calculate factor table */
  protected abstract void calcFactors();
  
  /** Is the factor table already built */
  public boolean isBuilt() {
    return !this.rows.isEmpty();
  }
  
  /** Build the factor table
   * @return true if build now, false if was already built
   */
  public boolean build() {
    if (this.isBuilt()) return false;
    for (Variable var: parents) var.build();
    calcFactors();
    return true;
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
      if (!vals.isEmpty()) sb.append("|\t");
      sb.append(value).append('\t');
      sb.append(p);
      return sb.toString();
    }
    
  }
}
