package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GraphicalModel {

  private final MallowsModel model;
  private final PreferenceSet pref;

  private final List<Variable> variables = new ArrayList<Variable>();
  
  public GraphicalModel(MallowsModel model, PreferenceSet pref) {
    this.model = model;
    this.pref = pref;
  }
  
  public MallowsModel getModel() {
    return model;
  }
  
  public void build() {
    Ranking reference = model.getCenter();
    HasseDiagram hasse = new HasseDiagram(pref);
    for (int i = 0; i < reference.length(); i++) {
      Item item = reference.get(i);
      if (pref.contains(item)) {
        
        hasse.add(item);
        PreferenceSet h = hasse.getPreferenceSet();
        Xii xii = getXii(item); // create insertion variable
        
        for (Item higher: h.getHigher(item)) {
          Xij xij = this.getXij(higher, i-1);
          xii.addParent(xij);
        }
        for (Item lower: h.getLower(item)) {
          Xij xij = this.getXij(lower, i-1);
          xii.addParent(xij);
        }
      }
    }
  }
  
  public List<Variable> getVariables() {
    return variables;
  }
  
  /** Get children of the variable */
  public Set<Variable> getChildren(Variable parent) {
    Set<Variable> children = new HashSet<Variable>();
    for (Variable var: variables) {
      if (var.getParents().contains(parent)) children.add(var);
    }
    return children;
  }
   
  /** Return Xii of the item. Creates one if it doesn't already exist. */
  public Xii getXii(Item item) {
    for (Variable var: variables) {
      if (var instanceof Xii) {
        Xii xii = (Xii) var;
        if (xii.getItem().equals(item)) return xii;
      }
    }
    
    Xii xii = new Xii(this, item);
    variables.add(xii);
    return xii;
  }
  
  /** Returns Xij of the item at time t. Creates one if it doesn't already exist. */
  public Xij getXij(Item item, int t) {
    for (Variable var: variables) {
      if (var instanceof Xij) {
        Xij xij = (Xij) var;
        if (xij.getItem().equals(item) && xij.getT() == t) return xij;
      }
    }
    
    Xij xij = new Xij(this, item, t);
    variables.add(xij);
    Xij before = getXijBefore(item, t);
    xij.addParent(before);
    return xij;
  }
  
  /** Returns the most recent Xij of the item before time t */
  public Xij getXijBefore(Item item, int t) {
    Xij before = null;
    for (Variable var: variables) {
      if (var instanceof Xij) {
        Xij xij = (Xij) var;
        if (xij.getItem().equals(item) && xij.getT() < t) {
          if (before == null || before.getT() < xij.getT()) before = xij;
        }
      }
    }
    return before;
  }
  
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Variable v: variables) sb.append(v);
    return sb.toString();
  }
  

}
