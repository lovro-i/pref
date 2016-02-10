package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import java.util.Set;


public class TopRanking extends Ranking {

  public TopRanking(ItemSet itemSet) {
    super(itemSet);
  }

  public TopRanking(TopRanking ranking) {
    super(ranking);
  }
  
  @Override
  public DensePreferenceSet transitiveClosure() {
    DensePreferenceSet tc = super.transitiveClosure();    
    
    Set<Item> missing = this.getMissingItems();
    for (int i = 0; i < size(); i++) {
      Item higher = get(i);
      for (Item lower: missing) {
        tc.add(higher, lower);        
      }      
    }
    
    return tc;
  }
  
}
