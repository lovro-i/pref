package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.core.Item;

/** Abstract class that represents custom pairwise preference set (not ranking) that can be pairwise edited
 */
public interface MutablePreferenceSet extends PreferenceSet {

  /** Adds this preference to the set. Returns true if added, returns false if the preference was already there.
   *  @throws IllegalStateException if attempted to add a pair that would make the graph cyclic.
   */
  public boolean add(Item higher, Item lower);
  
  /** Adds this preference to the set. Returns true if added, returns false if the preference was already there 
   *  @throws IllegalStateException if attempted to add a pair that would make the graph cyclic.
   */
  public boolean addById(int higherId, int lowerId);
  
  /** Adds this preference to the set. Returns true if added, returns false if the preference was already there 
   *  @throws IllegalStateException if attempted to add a pair that would make the graph cyclic.
   */
  public boolean addByTag(Object higherTag, Object lowerTag);
  
  /** Remove this pair from the set, whichever order
   * @return previous value
   */
  public Boolean remove(Item item1, Item item2);
    
  /** Remove this pair from the set, whichever order
   * @return previous value
   */
  public Boolean remove(int itemId1, int itemId2);
  
  
  /** Remove the preference from the set 
   * @return true if this preference was present and removed, false otherwise
   */
  public boolean remove(Preference pref);
    
  
  /** Transitively closes this preference set (without creating a new one) */
  public void transitiveClose();
  
}
