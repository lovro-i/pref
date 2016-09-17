package edu.drexel.cs.db.db4pref.gm;

import java.util.ArrayList;
import java.util.Collection;
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

  public double getProbability(int value, int... vals) {
    for (Row row: rows) {
      if (row.value != value) continue;
      if (vals.length != row.vals.size()) throw new IllegalArgumentException("Number of parameters does not match this factor table");
      boolean match = true;
      for (int i = 0; i < vals.length; i++) {
        if (vals[i] != row.vals.get(i)) {
          match = false;
          continue;
        }        
      }
      if (match) return row.p;
    }  
    return 0;
  }
  
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

  /**
   * Return set of values that this variable can have
   */
  public Set<Integer> getValues() {
    if (rows.isEmpty()) calcFactors();
    Set<Integer> vals = new HashSet<Integer>();
    for (Row row : rows) {
      vals.add(row.value);
    }
    return vals;
  }
  
  public void addRow(int value, double p, Integer... vals) {
    Row row = new Row(value, p, vals);
    this.rows.add(row);
  }

  /**
   * Calculate factor table
   */
  protected abstract void calcFactors();

  /**
   * Is the factor table already built
   */
  public boolean isBuilt() {
    return !this.rows.isEmpty();
  }

  /**
   * Build the factor table
   *
   * @return true if built just now, false if was already built
   */
  public boolean build() {
    if (this.isBuilt()) return false;
    for (Variable var : parents) var.build();
    calcFactors();
    return true;
  }



  protected class Row {

    protected final List<Integer> vals = new ArrayList<Integer>();
    protected final int value;
    protected final double p;

    protected Row(int value, double p, Collection<Integer> vals) {
      this.value = value;
      this.p = p;
      this.vals.addAll(vals);
    }
    
    protected Row(int value, double p, Integer... vals) {
      this.value = value;
      this.p = p;
      for (Integer i : vals) this.vals.add(i);
    }

    protected int[] getValues() {
      int[] values = new int[vals.size() + 1];
      int idx = 0;
      for (int i : vals) { //export parent values
        values[idx++] = i;
      }
      values[idx] = value; //export var value
      return values;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Integer i : vals) {
        sb.append(i + gm.getBase()).append('\t');
      }
      if (!vals.isEmpty()) sb.append("|\t");
      sb.append(value + gm.getBase()).append('\t');
      sb.append(p);
      return sb.toString();
    }

  }
  

}
