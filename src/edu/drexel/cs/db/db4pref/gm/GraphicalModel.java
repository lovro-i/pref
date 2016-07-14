package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.gm.Variable.Row;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphicalModel {

  private final MallowsModel model;
  private final Ranking reference;
  final Map<Item, Integer> referenceIndex;
  private final PreferenceSet pref;
  private final ItemSet items;
  private int base = 0;
  final Pos1 pos1;

  private final List<Variable> variables = new ArrayList<Variable>();

  public GraphicalModel(MallowsModel model, PreferenceSet pref) {
    this.model = model;
    this.reference = model.getCenter();
    this.referenceIndex = this.reference.getIndexMap();
    this.pref = pref;
    this.items = model.getItemSet();
    this.pos1 = new Pos1(this);
  }

  public MallowsModel getModel() {
    return model;
  }

  public void setOneBased(boolean one) {
    if (one) {
      this.base = 1;
    } else {
      this.base = 0;
    }
  }

  public int getBase() {
    return base;
  }

  public void alg2() {
    HasseDiagram hasse = new HasseDiagram(pref);
    for (int i = 0; i < reference.length(); i++) {
      Item item = reference.get(i);
      if (pref.contains(item)) {

        hasse.add(item);
        PreferenceSet h = hasse.getPreferenceSet();
        // Xii xii = GraphicalModel.this.createXii(item); // create insertion variable

        for (Item higher : h.getHigher(item)) {
          Xij xij = this.createXij(higher, i - 1);
          // xii.addParent(xij);
        }
        for (Item lower : h.getLower(item)) {
          Xij xij = this.createXij(lower, i - 1);
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
    for (Variable v : variables) {
      for (Variable p : v.getParents()) {
        edges.add(new Edge(p, v));
      }
    }
    return edges;
  }

  /**
   * Returns array of times at which each item is last seen
   */
  private Integer[] getLatest() {
    Integer[] latest = new Integer[items.size()];
    for (Variable v : variables) {
      if (v instanceof Xij) {
        Xij x = (Xij) v;
        int id = referenceIndex.get(x.getItem());
        if (latest[id] == null) {
          latest[id] = x.getTime();
        } else {
          latest[id] = Math.max(latest[id], x.getTime());
        }
      }
    }
    return latest;
  }

  public void alg3() {
    Integer[] latest = getLatest();

    Set<Xii> rims = new HashSet<Xii>();

    for (int i = 0; i < reference.length() - 1; i++) {
      // int idi = reference.get(i).id;
      if (latest[i] == null) {
        continue;
      }
      
      for (int j = i + 1; j < reference.length(); j++) {
        // int idj = reference.get(j).id;
        if (latest[j] == null) {
          continue;
        }
        int min = Math.min(latest[i], latest[j]);
        for (int k = j; k <= min; k++) {
          Xii xii = this.createXii(reference.get(k));
          rims.add(xii);
        }
      }
    }

    for (Item item : pref.getItems()) {
      Xii xii = this.createXii(item);
      rims.add(xii);
    }

    // for (Item item : reference.getItems()) {
      
    for (int i = 0; i < reference.length(); i++) {  
      if (latest[i] == null) continue;
      Item item = reference.get(i);

      for (int k = i + 1; k <= latest[i]; k++) {
        Xii xkk = this.getXii(k);
        if (xkk != null) {
          Xij xikm1 = getXij(item, k - 1);
          if (xikm1 == null) {
            Xij xil = getXijBefore(item, k - 1);
            xikm1 = createXij(item, k - 1); // pos1
            Logger.info("=== %s, %d, %d, %s, %s, %d", reference, reference.length(), items.size(), xil, item, k);
            xikm1.setPos1(xil);
          }

          Xij xik = createXij(item, k); // pos2
          xik.setPos2(xikm1, xkk);
        }
        else if (containsXij(item, k)) {
          Xij xil = getXijBefore(item, k);
          Xij xik = createXij(item, k);
          xik.setPos1(xil);
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
        for (Item higher : h.getHigher(item)) {
          Xij xij = getXij(higher, i - 1);
          if (xij != null) {
            parents.add(xij);
          }
        }

        Set<Xij> children = new HashSet<Xij>();
        for (Item lower : h.getLower(item)) {
          Xij xij = getXij(lower, i - 1);
          if (xij != null) {
            children.add(xij);
          }
        }

        Variable max = createMax(parents);
        Variable min = createMin(children);

        if (max != null && min != null) {
          xii.setIns(max, min);
        } else if (max != null) {
          xii.setAfter(max);
        } else if (min != null) {
          xii.setBefore(min);
        }
      }
    }
  }

  public void build() {
    this.alg2();
    this.alg3();
    this.alg4();
    for (Variable var : variables) {
      var.build();
    }
  }

  public List<Variable> getVariables() {
    return variables;
  }

  /**
   * Get children of the variable
   */
  public Set<Variable> getChildren(Variable parent) {
    Set<Variable> children = new HashSet<Variable>();
    for (Variable var : variables) {
      if (var.getParents().contains(parent)) {
        children.add(var);
      }
    }
    return children;
  }

  public Xii createXii(int id) {
    return GraphicalModel.this.createXii(reference.get(id));
  }

  /**
   * Return Xii of the item. Creates one if it doesn't already exist.
   */
  public Xii createXii(Item item) {
    Xii xii = getXii(item);
    if (xii == null) {
      xii = new Xii(this, item);
      variables.add(xii);
    }
    return xii;
  }

  public Xii getXii(Item item) {
    for (Variable var : variables) {
      if (var instanceof Xii) {
        Xii xii = (Xii) var;
        if (xii.getItem().equals(item)) {
          return xii;
        }
      }
    }
    return null;
  }

  public Xii getXii(int id) {
    return getXii(reference.get(id));
  }

  /**
   * Returns Xij of the item at time t. Creates one if it doesn't already exist.
   */
  public Xij createXij(Item item, int t) {
    if (t == referenceIndex.get(item)) {
      return createXii(item);
    }

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
    for (Variable var : variables) {
      if (var instanceof Xij) {
        Xij xij = (Xij) var;
        if (xij.getItem().equals(item) && xij.getTime() == t) {
          return xij;
        }
      }
    }
    return null;
  }

  public boolean containsXij(Item item, int t) {
    return getXij(item, t) != null;
  }

  private Variable createMax(Set<? extends Variable> parents) {
    Variable max = null;
    for (Variable var : parents) {
      if (max == null) {
        max = var;
      } else {
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
    for (Variable var : vars) {
      if (min == null) {
        min = var;
      } else {
        Min min1 = new Min(this);
        min1.addParent(min);
        min1.addParent(var);
        this.variables.add(min1);
        min = min1;
      }
    }
    return min;
  }

  /**
   * Returns the most recent Xij of the item before time t
   */
  public Xij getXijBefore(Item item, int t) {
    Xij before = null;
    for (Variable var : variables) {
      if (var instanceof Xij) {
        Xij xij = (Xij) var;
        if (xij.getItem().equals(item) && xij.getTime() < t) {
          if (before == null || before.getTime() < xij.getTime()) {
            before = xij;
          }
        }
      }
    }
    return before;
  }

  /**
   * Returns the latest Xij of the item
   */
  public Xij getLatestXij(Item item) {
    Xij latest = null;
    for (Variable var : variables) {
      if (var instanceof Xij) {
        Xij xij = (Xij) var;
        if (latest == null || latest.getTime() < xij.getTime()) {
          latest = xij;
        }
      }
    }
    return latest;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Graphical model for Mallows posterior ").append(pref).append("\n");
    sb.append(String.format("Variables: %d, edges: %d\n", this.getVariables().size(), this.getEdges().size()));
    for (Variable v : variables) {
      // variable id
      sb.append("\n[").append(v.getId()).append("]\n");

      // type
      if (v instanceof Xij) {
        sb.append("Type: ").append(((Xij) v).type).append('\n');
      }

      // variable parents
      sb.append("Parents: ");
      boolean first = true;
      for (Variable p : v.getParents()) {
        if (!first) {
          sb.append(", ");
        } else {
          first = false;
        }
        sb.append(p.getId());
      }
      sb.append('\n');

      // factor tables
      sb.append("Factor table:\n");
      for (Variable var : v.getParents()) {
        sb.append(var).append('\t');
      }
      if (!v.parents.isEmpty()) {
        sb.append("|\t");
      }
      sb.append(v).append("\tp\n");
      for (Row row : v.rows) {
        sb.append(row).append("\n");
      }
    }
    return sb.toString();
  }

  public void display() {
    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    Graph graph = new SingleGraph("Graphical Model for " + pref);
    String cssGraph = "graph { fill-color: white; padding: 80px; }";
    String cssNode = "node { fill-color: white; size: 80px; text-size: 20px; stroke-mode: plain; stroke-color: black; stroke-width: 3px; shape: circle; }";
    String cssEdge = "edge { arrow-size: 20px, 10px; size: 2px; }";
    graph.addAttribute("ui.stylesheet", cssGraph + cssNode + cssEdge);
    graph.addAttribute("ui.title", "Graphical model for Mallows posterior " + pref);

    for (Variable v : variables) {
      Node node = graph.addNode(v.getId());
      node.setAttribute("ui.label", v.getName());
    }

    int edgeId = 0;
    for (Variable v : variables) {
      for (Variable parent : v.getParents()) {
        edgeId++;
        graph.addEdge("Edge " + edgeId, parent.getId(), v.getId(), true);
      }
    }

    graph.display();
  }

  // sum of #rows in all CPTs
  public int getNetworkSize() {

    int networkSize = 0;
    Map<Variable, Range> ranges = new HashMap<Variable, Range>();

    for (Variable var : getVariables()) {
      Range range = new Range(var);
      ranges.put(var, range);
    }

    for (Variable var : getVariables()) {
      Range range = ranges.get(var);

      int size = range.size();
      for (Variable parent : var.getParents()) {
        size *= ranges.get(parent).size();
      }

      networkSize += size;
    }
    return networkSize;
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);

    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addByTag(4, 2);
    v.addByTag(4, 3);
    // v.addByTag(1, 5);
    // v.addByTag(2, 5);

    GraphicalModel gm = new GraphicalModel(model, v);
    gm.setOneBased(true);
    gm.build();
    gm.display();
    System.out.println(gm);
  }
}
