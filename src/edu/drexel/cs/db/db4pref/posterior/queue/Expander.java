package edu.drexel.cs.db.db4pref.posterior.queue;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.gm.HasseDiagram;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.Span;
import edu.drexel.cs.db.db4pref.posterior.sequential.Expander1;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.TestUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;


public class Expander {
  final MallowsModel model;
  final Ranking reference;
  final Map<Item, Integer> referenceIndex;
  
  final PreferenceSet pref;
  final MutablePreferenceSet tc;
  
  private double p;
  
  final State root;
  final int maxIndex;
  final Map<Item, Span> spans = new HashMap<>();
  final Map<State, Double> states = new HashMap<>();
  final PriorityQueue<State> queue;
  
  
  final Map<State, Map<State, Double>> expands;
  
  public Expander(MallowsModel model, PreferenceSet pref, Comparator<State> comparator, boolean cache) {
    this.model = model;
    this.reference = model.getCenter();
    this.pref = pref;
    this.tc = pref.transitiveClosure();
    this.referenceIndex = model.getCenter().getIndexMap();
    this.calculateSpans();
    this.root = new State(this);
    this.states.put(root, 1d);
    this.maxIndex = getMaxItem(pref);
    this.queue = new PriorityQueue<State>(comparator);
    if (cache) this.expands = new HashMap<>();
    else this.expands = null;
  }
  
  /** Returns the index of highest sigma_i in the preference set */
  private int getMaxItem(PreferenceSet pref) {
    int i = 0;
    for (Item item: pref.getItems()) {
      i = Math.max(i, referenceIndex.get(item));
    }
    return i;
  }
  
  private void calculateSpans() {
    for (Item item: pref.getItems()) {
      int from = referenceIndex.get(item);
      Span span = new Span(from, from);
      spans.put(item, span);
    }
    
    Ranking reference = model.getCenter();
    HasseDiagram hasse = new HasseDiagram(pref);
    for (int step = 0; step < reference.length(); step++) {
      Item item = reference.get(step);
      if (pref.contains(item)) {
        hasse.add(item);
        for (Preference p: hasse.getPreferenceSet()) {
          int il = referenceIndex.get(p.lower);
          int ih = referenceIndex.get(p.higher);
          if (il < ih && ih == step) spans.get(p.lower).setTo(step);
          else if (il > ih && il == step) spans.get(p.higher).setTo(step);
        }
      }
    }
  }
  
  
  private HashMap<Span, Double> probs = new HashMap<>();
  
  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  double probability(int itemIndex, int position) {
    Span span = new Span(itemIndex, position);
    Double p = probs.get(span);
    if (p != null) return p;
    
    double phi = model.getPhi();
    double p1 = Math.pow(phi, Math.abs(itemIndex - position)) * (1 - phi) / (1 - Math.pow(phi, itemIndex + 1));
    probs.put(span, p1);
    return p1;
  }
  
  private double expand() {
    this.p = 0;
    queue.clear();
    queue.add(root);
    states.clear();
    states.put(root, 1d);
    
    while (!queue.isEmpty()) {
      State state = queue.poll();
      double p = states.remove(state);
      
      // Logger.info("Got from queue state %s, %f", state, p);
            
      if (state.length() == this.maxIndex + 1) {
        this.p += p;
        // Logger.info("Adding leaf %s: %f (total %f)", state, p, this.p);
        continue;
      }
      
      Map<State, Double> children;
      
      if (expands != null) {
        children = expands.get(state);
        if (children == null) {
          children = state.expand();
          expands.put(state, children);
        }
      }
      else {
        children = state.expand();
      }
      
      for (Map.Entry<State, Double> entry: children.entrySet()) {
        State child = entry.getKey();
        double pc = entry.getValue() * p;
        if (states.containsKey(child)) {
          pc += states.get(child);
        }
        else {
          queue.add(child);
        }
        states.put(child, pc);
      }
    }
    return this.p;
  }
  
  
  public static void main(String[] args) throws InterruptedException, TimeoutException {
    // MapPreferenceSet pref = Test.pref4();
    MapPreferenceSet pref = TestUtils.generate(20, 4, 5);
    
//    ItemSet its = new ItemSet(10);
//    its.tagOneBased();
//    MapPreferenceSet pref = new MapPreferenceSet(its);  //its.getReferenceRanking();
//    pref.addById(2, 0);
    
    
    
    ItemSet items = pref.getItemSet();    
    items.tagOneBased();
    Logger.info(pref);

    double phi = 0.8;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    
    long t1, t2;
    
    {
      long startPref = System.currentTimeMillis();
      Expander1 pex = new Expander1(model, pref);
      double p = pex.expand();
      t1 = System.currentTimeMillis() - startPref;
      Logger.info("Expander1: Total probability: %f in %d ms", p, t1);
    }
    
    {
      long startPref = System.currentTimeMillis();
      Expander pex = new Expander(model, pref, DepthFirst.getInstance(), false);
      double p = pex.expand();
      t2 = System.currentTimeMillis() - startPref;
      Logger.info("Queue Expander no cache: Total probability: %f in %d ms <-", p, t2);
    }
    
    {
      long startPref = System.currentTimeMillis();
      Expander pex = new Expander(model, pref, DepthFirst.getInstance(), true);
      double p = pex.expand();
      t2 = System.currentTimeMillis() - startPref;
      Logger.info("Queue Expander with cache: Total probability: %f in %d ms <-", p, t2);
    }
    
    Logger.info("%.1f x slower", 1d * t2 / t1);
  }
  
}
