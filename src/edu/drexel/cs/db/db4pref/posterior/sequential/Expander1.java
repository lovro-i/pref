package edu.drexel.cs.db.db4pref.posterior.sequential;

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
import edu.drexel.cs.db.db4pref.posterior.parallel.Expander2;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.TestUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/** Best sequential so far (!!Check against sequential1) */
public class Expander1 {

  final MallowsModel model;
  final Map<Item, Integer> referenceIndex;
  
  PreferenceSet pref;
  MutablePreferenceSet tc;
  private Expands1 expands;
  Map<Item, Span> spans;

  
  /** Creates an Expander for the given Mallows model with specified number of threads */
  public Expander1(MallowsModel model) {
    this.model = model;
    this.referenceIndex = model.getCenter().getIndexMap();
  }
  
  
  public double getProbability(PreferenceSet pref) throws TimeoutException, InterruptedException {
    expand(pref);
    return expands.getProbability();
  }
  
  
  private void calculateSpans() {
    this.spans = new HashMap<Item, Span>();
    
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

  /** Returns the index of highest sigma_i in the preference set */
  private int getMaxItem(PreferenceSet pref) {
    int i = 0;
    for (Item item: pref.getItems()) {
      i = Math.max(i, referenceIndex.get(item));
    }
    return i;
  }
  
  public void expand(PreferenceSet pref) throws TimeoutException, InterruptedException {
    if (pref.equals(this.pref)) {
      Logger.info("Expander already available for PreferenceSet " + pref);
      return;
    }
    this.pref = pref;
    this.tc = pref.transitiveClosure();
    calculateSpans();
    expands = new Expands1(this);
    expands.nullify();
    Ranking reference = model.getCenter();
    
    int maxIndex = getMaxItem(pref);
    for (int i = 0; i <= maxIndex; i++) {
      Item item = reference.get(i);
      
      boolean missing = !this.pref.contains(item);
      expands = expands.insert(item, missing);
      // double ub = expands.getUpperBound(item);
      // Logger.info("Upper bound after item %s: %f", item, Math.log(expands.getUpperBound(item)));
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
  
  public static void main(String args[]) throws TimeoutException, InterruptedException {
    MapPreferenceSet pref = TestUtils.generate(20, 4, 5);
    
//    ItemSet its = new ItemSet(30);
//    its.tagOneBased();
//    MapPreferenceSet pref = new MapPreferenceSet(its);
//    pref.addByTag(24, 19);
//    pref.addByTag(26, 11);
//    pref.addByTag(25, 14);
//    pref.addByTag(25, 15);
//    pref.addByTag(22, 13);
    
//    pref.addByTag(18, 11);
//    pref.addByTag(4, 30);
//    pref.addByTag(25, 22);
//    pref.addByTag(16, 30);
//    pref.addByTag(1, 18);
    
    Logger.info(pref);
    ItemSet items = pref.getItemSet();    
    items.tagOneBased();

    double phi = 0.2;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);

//    {
//      long startPref = System.currentTimeMillis();
//      PreferenceExpander pex = new PreferenceExpander(model);
//      double p = pex.getProbability(pref);
//      Logger.info("PreferenceExpander: Total probability: %f in %d ms", Math.log(p), System.currentTimeMillis() - startPref);
//    }
    

    Logger.info("UB: %f", Math.log(model.getUpperBound(pref)/model.z()));

    {
      long startPref = System.currentTimeMillis();
      Expander2 pex = new Expander2(model, 1);
      double p = pex.getProbability(pref);
      Logger.info("Expander2: Total probability: %f in %d ms", Math.log(p), System.currentTimeMillis() - startPref);
    }

    {
      long startPref = System.currentTimeMillis();
      Expander1 pex = new Expander1(model);
      double p = pex.getProbability(pref);
      Logger.info("Expander1: Total probability: %f in %d ms", Math.log(p), System.currentTimeMillis() - startPref);
    }
    
//    for (int threads = 1; threads <= Runtime.getRuntime().availableProcessors(); threads++) {
//      long startPref = System.currentTimeMillis();
//      Expander2 pex = new Expander2(model, threads);
//      double p = pex.getProbability(pref);
//      Logger.info("Expander2 x%d: Total probability: %f in %d ms", threads, Math.log(p), System.currentTimeMillis() - startPref);
//    }
  }
  
}
