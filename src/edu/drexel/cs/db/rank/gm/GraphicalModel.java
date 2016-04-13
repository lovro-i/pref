package edu.drexel.cs.db.rank.gm;

import cern.colt.Arrays;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.gm.Variable.Row;
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
    
    Set<Xii> rims = new HashSet<Xii>();
    
    for (int i = 0; i < latest.length-1; i++) {
      if (latest[i] == null) continue;
      for (int j = i+1; j < latest.length; j++) {
        if (latest[j] == null) continue;
        int min = Math.min(latest[i], latest[j]);
        for (int k = j; k <= min; k++) {
          Xii xii = this.createXii(items.get(k));
          rims.add(xii);
        }
      }
    }

    for (Item item: pref.getItems()) {
      Xii xii = this.createXii(item);
      rims.add(xii);
    }
    
    for (Item item: reference.getItems()) {
      if (latest[item.id] == null) continue;
      for (int k = item.id + 1; k <= latest[item.id]; k++) {
        Xii xkk = this.getXii(k);
        if (xkk != null) {

          Xij xikm1 = getXij(item, k-1);
           if (xikm1 == null) {
             xikm1 = createXij(item, k-1);
             Xij xil = getXijBefore(item, k-1);
             xikm1.addParent(xil);
           }
           
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
  
  
  public void alg4() {
    HasseDiagram hasse = new HasseDiagram(pref);
    for (int i = 0; i < reference.length(); i++) {
      Item item = reference.get(i);
      if (pref.contains(item)) {
        hasse.add(item);
        PreferenceSet h = hasse.getPreferenceSet();
        Xii xii = this.createXii(item);
        
        Set<Xij> parents = new HashSet<Xij>();
        for (Item higher: h.getHigher(item)) {
          Xij xij = getXij(higher, i-1);
          if (xij != null) parents.add(xij);
        }
        
        Set<Xij> children = new HashSet<Xij>();
        for (Item lower: h.getLower(item)) {
          Xij xij = getXij(lower, i-1);
          if (xij != null) children.add(xij);
        }

        Variable max = createMax(parents);
        if (max != null) xii.addParent(max);
        
        Variable min = null;
        if (children.size() == 1) min = children.iterator().next();
        else if (children.size() > 1) min = createMin(children);
        if (min != null) xii.addParent(min);        
      }
    }
  }
  
  public void build() {
    this.alg2();
    this.alg3();
    this.alg4();
    for (Variable var: variables) {
      var.build();
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
  
  private Variable createMax(Set<? extends Variable> parents) {
    Variable max = null;
    for (Variable var: parents) {
      if (max == null) {
        max = var;
      }
      else {
        Max max1 = new Max(this);
        max1.addParent(max);
        max1.addParent(var);
        this.variables.add(max1);
        max = max1;
      }
    }
    return max;
  }
  
  private Variable createMin(Set<? extends Variable> vars) {
    Variable min = null;
    for (Variable var: vars) {
      if (min == null) {
        min = var;
      }
      else {
        Min min1 = new Min(this);
        min1.addParent(min);
        min1.addParent(var);
        this.variables.add(min1);
        min = min1;
      }
    }
    return min;
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
    sb.append("Graphical model for Mallows posterior ").append(pref).append("\n");
    sb.append(String.format("Variables: %d, edges: %d\n", this.getVariables().size(), this.getEdges().size()));
    for (Variable v: variables) {
      // variable name
      sb.append("\n[").append(v.getId()).append("]\n");
      
      // variable parents
      sb.append("Parents: ");
      boolean first = true;
      for (Variable p: v.getParents()) {
        if (!first) sb.append(", ");
        else first = false;
        sb.append(p.getId());
      }
      sb.append('\n');
      
      // factor tables
      sb.append("Factor table:\n");
      for (Row row: v.rows) {
        sb.append(row).append("\n");
      }
    }
    return sb.toString();
  }
  
  
  public void display() {
    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new SingleGraph("Graphical Model for " + pref);
    String cssNode = "node { fill-color: white; size: 60px; text-size: 20px; stroke-mode: plain; stroke-color: black; stroke-width: 3px; shape: circle; }";
    String cssEdge = "edge { arrow-size: 20px, 10px; size: 2px; }";
    graph.addAttribute("ui.stylesheet", cssNode + cssEdge);
    graph.addAttribute("ui.title", "Graphical model for Mallows posterior " + pref);
    
    for (Variable v: variables) {
      Node node = graph.addNode(v.getId());
      node.setAttribute("ui.label", v.getName());
    }
    
    int edgeId = 0;
    for (Variable v: variables) {
      for (Variable parent: v.getParents()) {
        edgeId++;
        graph.addEdge("Edge "+edgeId, parent.getId(), v.getId(), true);
      }
    }
    
		graph.display();
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.add(3, 1);
    v.add(3, 2);
    v.add(1, 5);
    v.add(2, 5);
    
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.build();
    gm.display();
    System.out.println(gm);
  }
}
