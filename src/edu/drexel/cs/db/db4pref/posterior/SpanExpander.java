package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.gm.Range;
import edu.drexel.cs.db.db4pref.sampler.triangle.UpTo;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.Utils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/** Main class of the Dynamic Algorithm. Expands the states and calculates the probabilities 
 * Version 2: Items are tracked only during span times; preference sets are converted to list of compatible rankings
 * @deprecated: Use PreferenceExpander instead
 */
@Deprecated
public class SpanExpander implements Posterior {

  /** Model that this Expander calculates */
  private final MallowsModel model;
  
  /** Just a utility map of item to its index in the reference ranking */
  final Map<Item, Integer> referenceIndex; 
  
  /** Partial order whose expand states are currently calculated */
  private Ranking ranking;
  
  /** Map of states to their probabilities */
  private SpanExpands expands;
  
  Map<Item, Span> spans;
  
  private int maxStates;
  private int totalStates;
  
  /** Timeout in milliseconds */
  long start;
  long timeout = Utils.ONE_HOUR;
  
  /** Creates an Expander for the give Mallows model */
  public SpanExpander(MallowsModel model) {
    this.model = model;
    this.referenceIndex = model.getCenter().getIndexMap();
  }
    
  public void setTimeout(long ms) {
    this.timeout = ms;
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
  public void expand(Ranking ranking) throws TimeoutException {
    if (ranking.equals(this.ranking)) {
      Logger.info("Expander already available for ranking " + ranking);
      return;
    }
    // Logger.info("Building expander for ranking " + ranking);
    this.ranking = ranking;
    this.maxStates = this.totalStates = 0;
    calculateSpans();
    expands = new SpanExpands(this);
    expands.nullify();
    Ranking reference = model.getCenter();
    
    for (int i = 0; i < reference.length(); i++) {
      Item e = reference.get(i);
      
      UpTo upto = new UpTo(ranking, i, referenceIndex); 
      int pos = upto.position;
      
      if (pos == -1) expands = expands.insertMissing(e);
      else expands = expands.insert(e, upto.previous);      
      
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
  public double getProbability(Ranking r) throws TimeoutException {
    this.start = System.currentTimeMillis();
    expand(r);
    rankingCount = 1;
    return expands.getProbability();
  }
  
  private int rankingCount = 0;
  
  /** How many rankings were expanded */
  public int getRankingCount() {
    return rankingCount;
  }
  
  public double getProbability(PreferenceSet pref) throws TimeoutException {
    this.start = System.currentTimeMillis();
    Set<Ranking> subRankings = pref.getRankings();
    double accumulatedProbability = 0;
    rankingCount = 0;
    for (Ranking r: subRankings) {
      if (System.currentTimeMillis() - start > timeout) throw new TimeoutException("Expander timeout exceeded");
      expand(r);
      accumulatedProbability += expands.getProbability();
      rankingCount++;
    }
    Logger.info("Expanded %d rankings", rankingCount);
    return accumulatedProbability;
  }
  
  /** Returns the probability of the specific sequence
   * @param seq Sequence whose probability we want */
  public double getProbability(Sequence seq) throws TimeoutException {
    this.start = System.currentTimeMillis();
    expand(seq.getRanking());
    rankingCount = 1;
    SpanExpand ex = new SpanExpand(seq);
    Double p = expands.get(ex);
    if (p == null) return 0;
    return p;
  } 

  /** Returns the Mallows model of this expander */
  public MallowsModel getModel() {
    return model;
  }
 
  
  public static void main(String args[]) throws TimeoutException {
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
