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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/** Main class of the Dynamic Algorithm. Expands the states and calculates the probabilities
 * Version 4: Items are tracked only during span times, and dynamically removed when not needed (static span + dynamic span); preference sets are expanded only once (from low to high on every level)
 */
public class DynamicPreferenceExpander implements Posterior {

  /** Model that this Expander calculates */
  private final MallowsModel model;
  
  /** Just a utility map of item to its index in the reference ranking */
  final Map<Item, Integer> referenceIndex; 
  
  /** Partial order whose expand states are currently calculated */
  private PreferenceSet pref;
  
  /** Timeout in milliseconds */
  long start;
  long timeout = Utils.ONE_HOUR;
  
  MutablePreferenceSet tc;
  
  /** Map of states to their probabilities */
  private DynamicPreferenceExpands expands;
  
  Map<Item, Span> spans;
  
  private int maxStates;
  private int totalStates;
  
  HashMap<Item, HashSet<Item>> lowers = new HashMap<>();
  HashMap<Item, HashSet<Item>> highers = new HashMap<>();

  
  /** Creates an Expander for the give Mallows model */
  public DynamicPreferenceExpander(MallowsModel model) {
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

  private void initHighersLowers() {
    for (Item item: tc.getItems()) {
      Set<Item> lower1 = tc.getLower(item);
      HashSet<Item> lower2 = new HashSet<Item>(lower1);
      this.lowers.put(item, lower2);
      Set<Item> higher1 = tc.getHigher(item);
      HashSet<Item> higher2 = new HashSet<Item>(higher1);
      this.highers.put(item, higher2);
    }
  }
  
  void pruneLowers(int step) {
    Iterator<Item> it = lowers.keySet().iterator();
    while (it.hasNext()) {
      Item item1 = it.next();
      int idx1 = this.referenceIndex.get(item1);
      if (idx1 >= step) continue;
      
      Iterator<Item> iter = lowers.get(item1).iterator();
      while (iter.hasNext()) {
        Item item2 = iter.next();
        int idx2 = this.referenceIndex.get(item2);
        if (idx2 < step) iter.remove();
      }
      
      if (lowers.get(item1).isEmpty()) it.remove();
    }
  }
  
  void pruneHighers(int step) {
    Iterator<Item> it = highers.keySet().iterator();
    while (it.hasNext()) {
      Item item1 = it.next();
      int idx1 = this.referenceIndex.get(item1);
      if (idx1 >= step) continue;
      
      Iterator<Item> iter = highers.get(item1).iterator();
      while (iter.hasNext()) {
        Item item2 = iter.next();
        int idx2 = this.referenceIndex.get(item2);
        if (idx2 < step) iter.remove();
      }
      
      if (highers.get(item1).isEmpty()) it.remove();
    }
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
    this.maxStates = this.totalStates = 0;
    calculateSpans();
    initHighersLowers();
    expands = new DynamicPreferenceExpands(this);
    expands.nullify();
    Ranking reference = model.getCenter();
    
    for (int i = 0; i < reference.length(); i++) {
      // Logger.info("Expanding item %d of %d", i+1, reference.length());
      pruneHighers(i);
      pruneLowers(i);
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
  
  public int getSumStates() {
    return totalStates;
  }
  
  /** Returns the sum of probabilities of all sequences with this partial ordering */
  @Override
  public double getProbability(Ranking r) throws TimeoutException {
    expand(r);
    return expands.getProbability();
  }
  
  @Override
  public double getProbability(PreferenceSet pref) throws TimeoutException{
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
 
  
  public static void main(String args[]) throws TimeoutException {
    ItemSet items = new ItemSet(15);    
    items.tagLetters();

    
    double phi = 0.85;
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

    // Logger.info(pref);
    
    long startDynPref = System.currentTimeMillis();
    DynamicPreferenceExpander pex = new DynamicPreferenceExpander(model);
    double pdp = Math.log(pex.getProbability(pref));
    Logger.info("DynamicPreferenceExpander: Total probability: %f in %d ms", pdp, System.currentTimeMillis() - startDynPref);
    
        
    long startPref = System.currentTimeMillis();
    PreferenceExpander sex = new PreferenceExpander(model);
    double pp = Math.log(sex.getProbability(pref));
    Logger.info("PreferenceExpander: Total probability: %f in %d ms\n\n", pp, System.currentTimeMillis() - startPref);
    

    if (pp != pdp) {
      Logger.info("ERROR! %f != %f", pp, pdp);
      Logger.waitKey();
    }
  }
  
}
