package edu.drexel.cs.db.db4pref.posterior.old;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.posterior.Posterior;
import edu.drexel.cs.db.db4pref.posterior.Sequence;
import edu.drexel.cs.db.db4pref.sampler.triangle.UpTo;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.Map;
import java.util.Set;

/** First unoptimized version of the Dynamic Algorithm. Expands the states and calculates the probabilities 
 * Version 1: Everything is expanded; preference sets are converted to list of compatible rankings
 * @deprecated: Use PreferenceExpander instead
 */
@Deprecated
public class FullExpander implements Posterior {

  /** Model that this Expander calculates */
  private final MallowsModel model;
  
  /** Just a utility map of item to its index in the reference ranking */
  final Map<Item, Integer> referenceIndex; 
  
  /** Partial order whose expand states are currently calculated */
  private Ranking ranking;
  
  /** Map of states to their probabilities */
  FullExpands expands;
  
  private int maxStates;
  
  /** Creates an Expander for the give Mallows model */
  public FullExpander(MallowsModel model) {
    this.model = model;
    this.referenceIndex = model.getCenter().getIndexMap();
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
    expands = new FullExpands(this);
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
  
  public double getProbability(PreferenceSet pref) {
    Set<Ranking> subRankings = pref.getRankings();
    double accumulatedProbability = 0;
    for (Ranking r: subRankings) {
      expand(r);
      accumulatedProbability += expands.getProbability();
    }
    return accumulatedProbability;
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
  
}
