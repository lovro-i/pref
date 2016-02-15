package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;


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
