package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;


public class GraphicalModel {

  private final MallowsModel model;
  private final PreferenceSet pref;
  private final ItemSet items;

  private final List<Variable> variables = new ArrayList<Variable>();
  
  public GraphicalModel(MallowsModel model, PreferenceSet pref) {
    this.model = model;
    this.pref = pref;
    this.items = model.getItemSet();
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
  
  /** Returns array of times at which each item is last seen */
  Integer[] getLatest() {
    Integer[] latest = new Integer[items.size()];
    for (Variable v: variables) {
      if (v instanceof Xij) {
        Xij x = (Xij) v;
        int id = x.getItem().id;
        if (latest[id] == null) latest[id] = x.getTime();
        else latest[id] = Math.max(latest[id], x.getTime());
      }
    }
    return latest;
  }
  
  
  public void enhance() {
    Integer[] latest = getLatest();
    
    Set<Xii> rims = new HashSet<Xii>();
    
    for (int i = 0; i < latest.length-1; i++) {
      if (latest[i] == null) continue;
      for (int j = i+1; j < latest.length; j++) {
        if (latest[j] == null) continue;
        int min = Math.min(latest[i], latest[j]);
        for (int k = j; k <= min; k++) {
          Xii xii = getXii(items.get(k));
          rims.add(xii);
        }
      }
    }
    
    for (Item item: items) {
      if (latest[item.id] == null) continue;
      
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
        if (xij.getItem().equals(item) && xij.getTime() == t) return xij;
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
        if (xij.getItem().equals(item) && xij.getTime() < t) {
          if (before == null || before.getTime() < xij.getTime()) before = xij;
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
  
  
  public void display() {
    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new SingleGraph("Graphical Model for " + pref);
    String cssNode = "node { fill-color: white; size: 50px; text-size: 20px; stroke-mode: plain; stroke-color: black; stroke-width: 3px; shape: circle; }";
    String cssEdge = "edge { arrow-size: 20px, 10px; size: 2px; }";
    graph.addAttribute("ui.stylesheet", cssNode + cssEdge);

    
    for (Variable v: variables) {
      graph.addNode(v.getName());
    }
    
    int edgeId = 0;
    for (Variable v: variables) {
      for (Variable parent: v.getParents()) {
        edgeId++;
        graph.addEdge("Edge "+edgeId, parent.getName(), v.getName(), true);
      }
    }
    
    for (Node node : graph) {
      node.addAttribute("ui.label", node.getId());        
    }
    
		graph.display();
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(25);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.add(0, 6);
    v.add(0, 9);
    v.add(0, 14);
    v.add(2, 8);
    v.add(2, 12);
    v.add(2, 15);
    v.add(4, 13);
    v.add(4, 20);
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.build();
    gm.enhance();
    gm.display();
  }
}
