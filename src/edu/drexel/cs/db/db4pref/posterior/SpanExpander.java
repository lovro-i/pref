package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.gm.Range;
import edu.drexel.cs.db.db4pref.sampler.triangle.UpTo;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Main class of the Dynamic Algorithm. Expands the states and calculates the probabilities */
public class SpanExpander implements Posterior {

  /** Model that this Expander calculates */
  private final MallowsModel model;
  
  /** Just a utility map of item to its index in the reference ranking */
  final Map<Item, Integer> referenceIndex; 
  
  /** Partial order whose expand states are currently calculated */
  private Ranking ranking;
  
  /** Map of states to their probabilities */
  private SpanMallowsExpands expands;
  
  Map<Item, Span> spans;
  
  private int maxStates;
  
  /** Creates an Expander for the give Mallows model */
  public SpanExpander(MallowsModel model) {
    this.model = model;
    this.referenceIndex = model.getCenter().getIndexMap();
  }
    
  
  private void calculateSpans() {
    this.spans = new HashMap<Item, Span>();
    for (int i = 0; i < ranking.length(); i++) {
      Item item = ranking.get(i);
      int from = referenceIndex.get(item);
      
      int to1 = from;
      int to2 = from;
      Item prev = (i == 0) ? null : ranking.get(i-1);
      Item next = (i < ranking.length() - 1) ? ranking.get(i+1) : null;
      if (prev != null) to1 = referenceIndex.get(prev);
      if (next != null) to2 = referenceIndex.get(next);
      int to = Math.max(to1, to2);
      Span span = new Span(from, to);
      spans.put(item, span);
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
  public void expand(Ranking ranking) {
    if (ranking.equals(this.ranking)) {
      Logger.info("Expander already available for ranking " + ranking);
      return;
    }
    // Logger.info("Building expander for ranking " + ranking);
    this.ranking = ranking;
    this.maxStates = 0;
    calculateSpans();
    expands = new SpanMallowsExpands(this);
    expands.nullify();
    Ranking reference = model.getCenter();
    
    for (int i = 0; i < reference.length(); i++) {
      Item e = reference.get(i);
      
      UpTo upto = new UpTo(ranking, i, referenceIndex); 
      int pos = upto.position;
      
      if (pos == -1) expands = expands.insertMissing(e);
      else expands = expands.insert(e, upto.previous);      
      
      maxStates = Math.max(maxStates, expands.size());
    }
  }
  
  public int getMaxStates() {
    return maxStates;
  }
  
  /** Returns the sum of probabilities of all sequences with this partial ordering */
  public double getProbability(Ranking r) {
    expand(r);
    return expands.getProbability();
  }
  
  public double getProbability(PreferenceSet pref){
    Set<Ranking> subRankings = pref.getRankings();
    double accumulatedProbability = 0;
    for (Ranking r: subRankings){
      expand(r);
      accumulatedProbability += expands.getProbability();
    }
    return accumulatedProbability;
  }
  
  /** Returns the probability of the specific sequence
   * @param seq Sequence whose probability we want */
  public double getProbability(Sequence seq) {
    expand(seq.getRanking());
    FullMallowsExpand ex = new FullMallowsExpand(seq);
    Double p = expands.get(ex);
    if (p == null) return 0;
    return p;
  } 

  /** Returns the Mallows model of this expander */
  public MallowsModel getModel() {
    return model;
  }
 
  
  public static void main(String args[]) {
    ItemSet items = new ItemSet(6); // Create a set of 6 items (elements, alternatives), IDs 0 to 5    
    items.tagLetters(); // Name items by letters (A, B, C...). Otherwise names (tags) of items are their IDs (zero based)
    // items.tagSigmas();
    // If you want to custom name items (or to assign any object that items represent), you can use Item.setTag(Object tag)
    // This loop names items sigma_1 to sigma_6. You have it one-based now
    // for (int i = 0; i < items.size(); i++) {
    //   items.get(i).setTag("sigma_" + (i+1));     
    // }
    
    
    double phi = 0.2;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi); // Mallows model defined by its reference ranking and phi
    
    // This is one ranking / partial order
    Ranking r = new Ranking(items);
    r.add(items.getItemById(4));
    r.add(items.getItemById(1));
    r.add(items.getItemById(2));
    
    // You can also access items by name:
    // Item sigma3 = items.getItemByTag("sigma_3");
    
    // Creating an Expander object that will calculate probabilities for this model
    FullExpander expander = new FullExpander(model);
    
    // Get the total probability of ranking r
    Logger.info("Expander: Total probability of partial order %s: %f", r, expander.getProbability(r));
    
    
    SpanExpander sex = new SpanExpander(model);
    Logger.info("SmartExpander: Total probability of partial order %s: %f", r, sex.getProbability(r));
  }
}
