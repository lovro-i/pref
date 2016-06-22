package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.filter.MissingProbabilities;
import edu.drexel.cs.db.db4pref.gm.HasseDiagram;
import edu.drexel.cs.db.db4pref.sampler.triangle.UpTo;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Main class of the Dynamic Algorithm. Expands the states and calculates the probabilities */
public class PreferenceExpander implements Posterior {

  /** Model that this Expander calculates */
  private final MallowsModel model;
  
  /** Just a utility map of item to its index in the reference ranking */
  final Map<Item, Integer> referenceIndex; 
  
  /** Partial order whose expand states are currently calculated */
  private PreferenceSet pref;
  
  MutablePreferenceSet tc;
  
  /** Map of states to their probabilities */
  private PreferenceExpands expands;
  
  Map<Item, Span> spans;
  
  private int maxStates;
  private int totalStates;
  
  /** Creates an Expander for the give Mallows model */
  public PreferenceExpander(MallowsModel model) {
    this.model = model;
    this.referenceIndex = model.getCenter().getIndexMap();
  }
    
  
  void calculateSpans() {
    this.spans = new HashMap<Item, Span>();
    
    for (Item item: pref.getItems()) {
      int from = referenceIndex.get(item);
      Span span = new Span(from, from);
      spans.put(item, span);
    }
    
    HasseDiagram hasse = new HasseDiagram(pref);
    for (Item item: model.getCenter().getItems()) {
      if (pref.contains(item)) {
        int step = referenceIndex.get(item);
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
  
  /** Returns number of items that are relevant at this step */
  public int getS(int step) {
    int s = 0;
    for (Span span: spans.values()) {
      if (step >= span.from && step <= span.to) s++;
    }
    return s;
  }
  
  public int getWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w = Math.max(w, getS(i));
    }
    return w;
  }
  

  /** Executes the dynamic algorithm for this partial order. 
   * Does not have to be called explicitely, it will be called from getProbability() methods when needed (when it's not calculated for the specified ranking).
   */
  public void expand(PreferenceSet pref) {
    if (pref.equals(this.pref)) {
      Logger.info("Expander already available for PreferenceSet " + pref);
      return;
    }
    // Logger.info("Building expander for ranking " + ranking);
    this.pref = pref;
    this.tc = pref.transitiveClosure();
    this.maxStates = this.totalStates = 0;
    calculateSpans();
    expands = new PreferenceExpands(this);
    expands.nullify();
    Ranking reference = model.getCenter();
    
    for (int i = 0; i < reference.length(); i++) {
      Item item = reference.get(i);
      
      if (this.pref.contains(item)) expands = expands.insert(item);
      else expands = expands.insertMissing(item);
      
      maxStates = Math.max(maxStates, expands.size());
      totalStates += expands.size();
    }
  }
  
  public int getMaxStates() {
    return maxStates;
  }
  
  public int getTotalStates() {
    return totalStates;
  }
  
  /** Returns the sum of probabilities of all sequences with this partial ordering */
  public double getProbability(Ranking r) {
    expand(r);
    return expands.getProbability();
  }
  
  public double getProbability(PreferenceSet pref){
    expand(pref);
    return expands.getProbability();
  }
  
  /** Returns the probability of the specific sequence
   * @param seq Sequence whose probability we want */
  public double getProbability(Sequence seq) {
    expand(seq.getRanking());
    FullExpand ex = new FullExpand(seq);
    Double p = expands.get(ex);
    if (p == null) return 0;
    return p;
  } 

  /** Returns the Mallows model of this expander */
  public MallowsModel getModel() {
    return model;
  }
 
  
  public static void main(String args[]) {
    ItemSet items = new ItemSet(6);    
    items.tagLetters();

    
    double phi = 0.2;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    
    // This is one ranking / partial order
//    MapPreferenceSet pref = new MapPreferenceSet(items);
//    pref.addById(1, 0);
//    pref.addById(1, 3);
//    pref.addById(2, 4);
//    pref.addById(4, 5);
    
    Ranking r = items.getRandomRanking();
    MapPreferenceSet pref = r.transitiveClosure();
    Filter.removePreferences(pref, MissingProbabilities.uniform(items, 0.5));
//    Filter.removeItems(r, 0.5);
//    PreferenceSet pref = r;
    
//    long startFull = System.currentTimeMillis();
//    FullExpander expander = new FullExpander(model);
//    Logger.info("FullExpander: Total probability of %s: %f in %d ms", pref, expander.getProbability(pref), System.currentTimeMillis() - startFull);

    Logger.info(pref);

    long startPref = System.currentTimeMillis();
    PreferenceExpander pex = new PreferenceExpander(model);
    Logger.info("PreferenceExpander: Total probability: %f in %d ms", pex.getProbability(pref), System.currentTimeMillis() - startPref);
    
    long startSpan = System.currentTimeMillis();
    SpanExpander sex = new SpanExpander(model);
    Logger.info("SpanExpander: Total probability: %f in %d ms\n\n", sex.getProbability(pref), System.currentTimeMillis() - startSpan);
    
    
  }
  
}
