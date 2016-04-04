package edu.drexel.cs.db.rank.top;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import java.util.HashSet;
import java.util.Set;


public class TopRanking extends Ranking {

  public TopRanking(ItemSet itemSet) {
    super(itemSet);
  }

  public TopRanking(TopRanking ranking) {
    super(ranking);
  }
  
  @Override
  public Boolean isPreferred(Item higher, Item lower) {
    throw new IllegalArgumentException("ToDo");
  }
  
  @Override
  public Boolean isPreferred(int higher, int lower) {
    throw new IllegalArgumentException("ToDo");
  }
  
  @Override
  public Set<Item> getHigher(Item item) {
    if (this.contains(item)) return super.getHigher(item);
    return new HashSet<Item>(this.items);
  }

  @Override
  public Set<Item> getLower(Item item) {
    Set<Item> lower;
    if (this.contains(item)) {
      lower = super.getLower(item);
      for (Item missing: this.getMissingItems()) lower.add(missing);
    }
    else {
      lower = new HashSet<Item>();
    }
    return lower;
  }
  
  public int size() {
    int n = this.length();
    int m = this.itemSet.size() - n;
    return n * (n - 1) / 2 + n * m;
  }
  
  @Override
  public MapPreferenceSet transitiveClosure() {
    MapPreferenceSet tc = super.transitiveClosure();    
    
    Set<Item> missing = this.getMissingItems();
    for (int i = 0; i < length(); i++) {
      Item higher = get(i);
      for (Item lower: missing) {
        tc.add(higher, lower);        
      }      
    }
    
    return tc;
  }
  
}
