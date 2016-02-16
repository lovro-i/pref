package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Absract class that represents custom pairwise preference set (not ranking) that can be pairwise edited
 */
public interface MutablePreferenceSet extends PreferenceSet {

  /** Adds this preference to the set. Returns true if added, returns false if the preference was already there */
  public boolean add(Item higher, Item lower);
  
  /** Adds this preference to the set. Returns true if added, returns false if the preference was already there */
  public boolean add(int higherId, int lowerId);
  
  /** Remove this pair from the set, whichever order
   * @return previous value
   */
  public Boolean remove(Item item1, Item item2);
  
  /** Remove this pair from the set, whichever order
   * @return previous value
   */
  public Boolean remove(int idemId1, int itemId2);  
  
}
