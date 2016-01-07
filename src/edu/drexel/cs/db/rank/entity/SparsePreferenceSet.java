package edu.drexel.cs.db.rank.entity;

import java.util.HashSet;


public class SparsePreferenceSet extends HashSet<Preference> implements PreferenceSet {

  private final ElementSet elements;

  public SparsePreferenceSet(ElementSet elements) {
    this.elements = elements;
  }

  public void add(Element higher, Element lower) {
    if (!elements.contains(higher)) throw new IllegalArgumentException("Element " + higher + " not in the set");
    if (!elements.contains(lower)) throw new IllegalArgumentException("Element " + lower + " not in the set");
    
    Preference pref = new Preference(higher, lower);
    this.add(pref);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Preference pref: this) {
      sb.append(pref).append("\n");
    }
    sb.append("===== ").append(this.size()).append(" preference pairs =====");
    return sb.toString();
  }
}
