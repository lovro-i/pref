package edu.drexel.cs.db.db4pref.posterior.app;

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
import edu.drexel.cs.db.db4pref.posterior.sequential.State1;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.TestUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

public class Expander implements Comparator<StateKey> {

  final MallowsModel model;
  final Ranking reference;
  final Map<Item, Integer> referenceIndex;
  
  final PreferenceSet pref;
  final MutablePreferenceSet tc;
  final Map<Item, Span> spans = new HashMap<>();
  final Map<StateKey, StateData> states = new HashMap<>();
  final StateKey root;
  final int maxIndex;
  final Set<StateData> leaves = new HashSet<StateData>();
  final TreeSet<StateKey> queue = new TreeSet<StateKey>(this);
  final Set<StateKey> done = new HashSet<StateKey>();
  
  
  public Expander(MallowsModel model, PreferenceSet pref) {
    this.model = model;
    this.reference = model.getCenter();
    this.pref = pref;
    this.tc = pref.transitiveClosure();
    this.referenceIndex = model.getCenter().getIndexMap();
    this.calculateSpans();
    this.root = new StateKey(this);
    this.states.put(root, new StateData());
    this.maxIndex = getMaxItem(pref);
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
  
  public StateData getState(StateKey state) {
    return states.get(state);
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
  
  
  private void exec() {
    queue.add(root);
    
    int count = 1;
    while (!queue.isEmpty()) {
      StateKey state = queue.pollFirst();
      done.add(state);
      StateData data = states.get(state);
            
      if (state.length() == this.maxIndex + 1) {
        leaves.add(data);
        continue;
      }
      
      boolean expanded = state.expand();
      if (expanded) {
        count++;
        for (Entry<StateKey, Double> child: data.children.entrySet()) {
          StateKey childState = child.getKey();
          if (!done.contains(childState)) queue.add(childState);
        }
      }
      // states.remove(state);
    }
    Logger.info("States through queue: %d", count);
  }
  
  public double getProbability() {
    double p = 0;
    for (StateData data: leaves) {
      p += data.p;
    }
    return p;
  }
  
  public void add(StateKey state, double p) {
    StateData data = states.get(state);
    if (data == null) {
      data = new StateData(p);
      states.put(state, data);
    }
    else {
      data.p += p;
    }
  }
  
  public void set(StateKey state, double p) {
    StateData data = states.get(state);
    if (data == null) {
      data = new StateData(p);
      states.put(state, data);
    }
    else {
      data.p = p;      
    }
  }
  
 
  
  @Override
  public int compare(StateKey state1, StateKey state2) {
    if (state1.equals(state2)) return 0;
    
    int len1 = state1.length();
    int len2 = state2.length();
    if (len1 > len2) return -1;
    else if (len2 > len1) return 1;
    
    double p1 = states.get(state1).p;
    double p2 = states.get(state2).p;
    if (p1 > p2) return -1;
    else if (p1 < p2) return 1;
    
    int diff = state1.miss.length - state2.miss.length;
    if (diff != 0) return diff;
    
    for (int i = 0; i < state1.miss.length; i++) {
      diff = state1.miss[i] - state2.miss[i];
      if (diff != 0) return diff;      
    }
    
    for (int i = 0; i < state1.items.length; i++) {
      diff = state1.items[i].compareTo(state2.items[i]);
      if (diff != 0) return diff;
    }
    return 0;
  }
  
  
  public static void main(String[] args) throws TimeoutException, InterruptedException {
    MapPreferenceSet pref = TestUtils.generate(30, 4, 5);
    
//    ItemSet its = new ItemSet(5);
//    its.tagOneBased();
//    MapPreferenceSet pref = new MapPreferenceSet(its);  //its.getReferenceRanking();
//    pref.addById(4, 0);
//    pref.addById(2, 3);
    //pref.addById(4, 3);
    
    
    ItemSet items = pref.getItemSet();    
    items.tagOneBased();
    Logger.info(pref);

    double phi = 0.8;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    
    long t1, t2;
    
    {
      long startPref = System.currentTimeMillis();
      Expander1 pex = new Expander1(model);
      double p = pex.getProbability(pref);
      Logger.info("States total: %d", State1.count);
      t1 = System.currentTimeMillis() - startPref;
      Logger.info("Expander1: Total probability: %f in %d ms", Math.log(p), t1);
    }
    
    {
      long startPref = System.currentTimeMillis();
      Expander pex = new Expander(model, pref);
      Writer writer = new Writer(pex);
      writer.start();
      pex.exec();
      writer.done();
      double p = pex.getProbability();
      t2 = System.currentTimeMillis() - startPref;
      Logger.info("Expander app: Total probability: %f in %d ms <-", Math.log(p), t2);
    }
    
    Logger.info("%.1f x slower", 1d * t2 / t1);
  }


  static class Writer extends Thread {
    
    private volatile boolean done;
    private Expander expander;
    
    private Writer(Expander expander) {
      this.expander = expander;
    }
    
    
    public void done() {
      this.done = true;
    }
    
    public void run() {
      long start = System.currentTimeMillis();
      try {
        while (true) {
          sleep(1000);
          if (done) break;
          Logger.info("Probability after %d ms: %f", System.currentTimeMillis() - start, Math.log(expander.getProbability()));
        }
      } catch (InterruptedException ex) {}
    }  
  }
  
}


