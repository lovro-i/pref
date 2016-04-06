package edu.drexel.cs.db.rank.gm;

import cern.colt.Arrays;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;


public class GraphicalModel {

  private final MallowsModel model;
  private final Ranking reference;
  private final PreferenceSet pref;
  private final ItemSet items;

  private final List<Variable> variables = new ArrayList<Variable>();
  
  public GraphicalModel(MallowsModel model, PreferenceSet pref) {
    this.model = model;
    this.reference = model.getCenter();
    this.pref = pref;
    this.items = model.getItemSet();
  }
  
  public MallowsModel getModel() {
    return model;
  }
  
  public void alg2() {
    HasseDiagram hasse = new HasseDiagram(pref);
    for (int i = 0; i < reference.length(); i++) {
      Item item = reference.get(i);
      if (pref.contains(item)) {
        
        hasse.add(item);
        PreferenceSet h = hasse.getPreferenceSet();
        // Xii xii = GraphicalModel.this.createXii(item); // create insertion variable
        
        for (Item higher: h.getHigher(item)) {
          Xij xij = this.createXij(higher, i-1);
          // xii.addParent(xij);
        }
        for (Item lower: h.getLower(item)) {
          Xij xij = this.createXij(lower, i-1);
          // xii.addParent(xij);
        }
      }
    }
  }
  
  public static class Edge {
    public final Variable from;
    public final Variable to;
    
    private Edge(Variable from, Variable to) {
      this.from = from;
      this.to = to;
    }
  }
  
  public Set<Edge> getEdges() {
    Set<Edge> edges = new HashSet<Edge>();
    for (Variable v: variables) {
      for (Variable p: v.getParents()) {
        edges.add(new Edge(p, v));
      }
    }
    return edges;
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
  
  
  public void alg3() {
    Integer[] latest = getLatest();
    System.out.println(Arrays.toString(latest));
    
    Set<Xii> rims = new HashSet<Xii>();
    
    for (int i = 0; i < latest.length-1; i++) {
      if (latest[i] == null) continue;
      for (int j = i+1; j < latest.length; j++) {
        if (latest[j] == null) continue;
        int min = Math.min(latest[i], latest[j]);
        for (int k = j; k <= min; k++) {
          Xii xii = GraphicalModel.this.createXii(items.get(k));
          rims.add(xii);
        }
      }
    }
    
    for (Item item: items) {
      if (latest[item.id] == null) continue;
      for (int k = item.id + 1; k <= latest[item.id]; k++) {
        Xii xkk = this.getXii(k);
        if (xkk != null) {
           Xij xikm1 = createXij(item, k-1);
           Xij xik = createXij(item, k);
           xik.addParent(xikm1);
           xik.addParent(xkk);
        }
        else if (containsXij(item, k)) {
          Xij xil = getXijBefore(item, k);
          Xij xik = createXij(item, k);
          xik.addParent(xil);
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
   
  public Xii createXii(int id) {
    return GraphicalModel.this.createXii(items.get(id));
  }
  
  /** Return Xii of the item. Creates one if it doesn't already exist. */
  public Xii createXii(Item item) {
    Xii xii = getXii(item);
    if (xii == null) {
      xii = new Xii(this, item);
      variables.add(xii);
    }
    return xii;
  }
  
  public Xii getXii(Item item) {
    for (Variable var: variables) {
      if (var instanceof Xii) {
        Xii xii = (Xii) var;
        if (xii.getItem().equals(item)) return xii;
      }
    }
    return null;
  }
  
  public Xii getXii(int id) {
    return getXii(items.get(id));
  }
  
  /** Returns Xij of the item at time t. Creates one if it doesn't already exist. */
  public Xij createXij(Item item, int t) {
    if (t == reference.indexOf(item)) return createXii(item);
    
    Xij xij = getXij(item, t);
    if (xij == null) {
      xij = new Xij(this, item, t);
      variables.add(xij);
      // Xij before = getXijBefore(item, t);
      // xij.addParent(before);
    }
    return xij;
  }
  
  public Xij getXij(Item item, int t) {
    for (Variable var: variables) {
      if (var instanceof Xij) {
        Xij xij = (Xij) var;
        if (xij.getItem().equals(item) && xij.getTime() == t) return xij;
      }
    }
    return null;
  }
  
  public boolean containsXij(Item item, int t) {
    return getXij(item, t) != null;
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
  
  /** Returns the latest Xij of the item */
  public Xij getLatestXij(Item item) {
    Xij latest = null;
    for (Variable var: variables) {
      if (var instanceof Xij) {
        Xij xij = (Xij) var;
        if (latest == null || latest.getTime() < xij.getTime()) latest = xij;
      }
    }
    return latest;
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
    String cssNode = "node { fill-color: white; size: 60px; text-size: 20px; stroke-mode: plain; stroke-color: black; stroke-width: 3px; shape: circle; }";
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
    // items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.add(3, 7);
    v.add(3, 5);
    v.add(3, 20);
    v.add(5, 2);
//    v.add(0, 6);
//    v.add(0, 9);
//    v.add(0, 14);
//    v.add(2, 8);
//    v.add(2, 12);
//    v.add(2, 15);
//    v.add(4, 13);
//    v.add(4, 20);
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.alg2();
    gm.alg3();
    gm.display();
  }
}
