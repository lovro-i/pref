package edu.drexel.cs.db.db4pref.posterior.sequential1;

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
import edu.drexel.cs.db.db4pref.posterior.app.Test;
import edu.drexel.cs.db.db4pref.posterior.sequential.Expander1;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

/** Version that uses Queue for all states, breath first.
 * It was a step towards custom scheduling (depth first, too), but turns out to be faster than old sequential (check this!)
 * @author Lovro
 */
public class Expander {

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
  final TreeSet<StateKey> queue = new TreeSet<StateKey>();
  
  
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
  
  
  public void exec() {
    queue.add(root);
    
    while (!queue.isEmpty()) {
      StateKey state = queue.pollFirst();
      StateData data = states.get(state);
            
      if (state.length() == this.maxIndex + 1) {
        leaves.add(data);
        continue;
      }
      
      boolean expanded = state.expand();
      if (expanded) {
        queue.addAll(data.children);
      }
      states.remove(state);
    }
  }
  
  public double getProbability() {
    if (leaves.isEmpty()) exec();
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
  
  public static void main(String[] args) throws TimeoutException, InterruptedException {
    MapPreferenceSet pref = Test.pref3(); //TestUtils.generate(30, 4, 5);
    
//    ItemSet its = new ItemSet(3);
//    its.tagOneBased();
//    Ranking pref = new Ranking(its);  //its.getReferenceRanking();
//    pref.add(its.get(2));
//    pref.add(its.get(0));
    
    Logger.info(pref);
    ItemSet items = pref.getItemSet();    
    items.tagOneBased();

    double phi = 0.8;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    
    {
      long startPref = System.currentTimeMillis();
      Expander1 pex = new Expander1(model);
      double p = pex.getProbability(pref);
      Logger.info("Expander1: Total probability: %f in %d ms", Math.log(p), System.currentTimeMillis() - startPref);
    }
    
    {
      long startPref = System.currentTimeMillis();
      Expander pex = new Expander(model, pref);
      double p = pex.getProbability();
      Logger.info("Expander app: Total probability: %f in %d ms", Math.log(p), System.currentTimeMillis() - startPref);
    }
  }
  
}
