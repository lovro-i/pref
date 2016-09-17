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
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.Utils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/** Main class of the Dynamic Algorithm. Expands the states and calculates the probabilities 
  * Version 3: Items are tracked only during span times (same as Span version); preference sets are expanded only once (from low to high on every level)
 */
public class PreferenceExpander implements Posterior {

  /** Model that this Expander calculates */
  private final MallowsModel model;
  
  /** Just a utility map of item to its index in the reference ranking */
  final Map<Item, Integer> referenceIndex; 
  
  /** Partial order whose expand states are currently calculated */
  private PreferenceSet pref;
  
  /** Timeout in milliseconds */
  long start;
  long timeout = 0;
  
  MutablePreferenceSet tc;
  
  /** Map of states to their probabilities */
  private PreferenceExpands expands;
  
  Map<Item, Span> spans;
  
  private int maxStates;
  private long totalStates;
  
  /** Creates an Expander for the give Mallows model */
  public PreferenceExpander(MallowsModel model) {
    this.model = model;
    this.referenceIndex = model.getCenter().getIndexMap();
  }
    
  public void setTimeout(long ms) {
    this.timeout = ms;
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
  
  /** Returns number of items that are relevant at the given step */
  public int getS(int step) {
    int s = 0;
    for (Span span: spans.values()) {
      if (step >= span.from && step <= span.to) s++;
    }
    return s;
  }
  
  public int getMaxWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w = Math.max(w, getS(i));
    }
    return w;
  }
  
  public int getSumWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w += getS(i);
    }
    return w;
  }
  
  public long getProductWidth() {
    long w = 1;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      int s = this.getS(i);
      if (s > 0) w *= s;
    }
    return w;
  }
  
  /** Returns the index of highest sigma_i in the preference set */
  private int getMaxItem(PreferenceSet pref) {
    int i = 0;
    for (Item item: pref.getItems()) {
      i = Math.max(i, referenceIndex.get(item));
    }
    return i;
  }

  /** Executes the dynamic algorithm for this partial order. 
   * Does not have to be called explicitely, it will be called from getProbability() methods when needed (when it's not calculated for the specified ranking).
   */
  public void expand(PreferenceSet pref) throws TimeoutException {
    if (pref.equals(this.pref)) {
      Logger.info("Expander already available for PreferenceSet " + pref);
      return;
    }
    // Logger.info("Building expander for ranking " + ranking);
    this.start = System.currentTimeMillis();
    this.pref = pref;
    this.tc = pref.transitiveClosure();
    this.maxStates = 0;
    this.totalStates = 0;
    calculateSpans();
    expands = new PreferenceExpands(this);
    expands.nullify();
    Ranking reference = model.getCenter();
    
    int maxIndex = getMaxItem(pref);
    for (int i = 0; i <= maxIndex; i++) {
      
      Item item = reference.get(i);
      
      if (this.pref.contains(item)) expands = expands.insert(item);
      else expands = expands.insertMissing(item);
      
      maxStates = Math.max(maxStates, expands.size());
      // Logger.info("Expanded item %d of %d: %d states", i+1, reference.length(), expands.size());
      // if (expands.size() < 100) Logger.info(expands);
      totalStates += expands.size();
    }
  }
  
  
  
  public int getMaxStates() {
    return maxStates;
  }
  
  public long getSumStates() {
    return totalStates;
  }
  
  /** Returns the sum of probabilities of all sequences with this partial ordering */
  @Override
  public double getProbability(Ranking r) throws TimeoutException {
    expand(r);
    return expands.getProbability();
  }
  
  @Override
  public double getProbability(PreferenceSet pref) throws TimeoutException {
    expand(pref);
    return expands.getProbability();
  }
  
  /** Returns the probability of the specific sequence
   * @param seq Sequence whose probability we want */
  public double getProbability(Sequence seq) throws TimeoutException {
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
 
  private HashMap<Span, Double> probs = new HashMap<>();

  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  double probability(int itemIndex, int position) {
    Span span = new Span(itemIndex, position);
    if (probs.containsKey(span)) {
      return probs.get(span);
    }
    else {
      double phi = getModel().getPhi();
      double p = Math.pow(phi, Math.abs(itemIndex - position)) * (1 - phi) / (1 - Math.pow(phi, itemIndex + 1));
      probs.put(span, p);
      return p;
    }
  }

  
  public static void main(String args[]) throws TimeoutException {
    ItemSet items = new ItemSet(20);    
    items.tagOneBased();

    
    double phi = 0.8;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    
    // This is one ranking / partial order
//    MapPreferenceSet pref = new MapPreferenceSet(items);
//    pref.addById(1, 0);
//    pref.addById(1, 3);
//    pref.addById(2, 4);
//    pref.addById(4, 5);
    
    Ranking r = items.getRandomRanking();
    MapPreferenceSet pref = r.transitiveClosure();
    Filter.removePreferences(pref, MissingProbabilities.uniform(items, 0.8));
    Logger.info(pref);

    long startPref = System.currentTimeMillis();
    PreferenceExpander pex = new PreferenceExpander(model);
    Logger.info("PreferenceExpander: Total probability: %f in %d ms", pex.getProbability(pref), System.currentTimeMillis() - startPref);
  }
  
}
