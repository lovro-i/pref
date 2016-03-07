package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.List;


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
    for (int i = 0; i < reference.size(); i++) {      
      Item item = reference.get(i);
      if (pref.contains(item)) {
        Xii xii = getXii(item);
        for (Item higher: pref.getHigher(item)) {
          int i1 = reference.indexOf(higher);
          if (i1 < i) {
            Xij xij = this.getXij(higher, i-1);
            xii.addParent(xij);
          }
        }
        for (Item lower: pref.getLower(item)) {
          int i1 = reference.indexOf(lower);
          if (i1 < i) {
            Xij xij = this.getXij(lower, i-1);
            xii.addParent(xij);
          }
        }
      }
    }
  }
  
  public List<Variable> getChildren(Variable parent) {
    List<Variable> children = new ArrayList<Variable>();
    for (Variable var: variables) {
      if (var.getParents().contains(parent)) children.add(var);
    }
    return children;
  }
    
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
  
  
  
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    items.letters();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    // BD
    Ranking r = new Ranking(items);
    r.add(items.get(1));
    r.add(items.get(3));
    
    GraphicalModel gm = new GraphicalModel(model, r);
    gm.build();
    System.out.println(gm);
    
  }
}
