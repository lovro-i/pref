package edu.drexel.cs.db.db4pref.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/** Set of preferences of one user */
public interface PreferenceSet extends Cloneable, Serializable {

  /** Returns the set of items */
  public ItemSet getItemSet();
  
  /** Returns the set of all items that are appearing in this PreferenceSet */
  public Collection<Item> getItems();
  
  /** Returns true if contains this preference, false if contains the opposite, and null if it doesn't contain the information on this pair */
  public Boolean isPreferred(Item preferred, Item over);
  
  /** Returns true if contains preference with this item IDs, false if contains the opposite, and null if it doesn't contain the information on this pair */
  public Boolean isPreferred(int preferred, int over);
  
  /** Returns the transitive closure of this preference set, as MutablePreferenceSet */
  public MutablePreferenceSet transitiveClosure();
  
  /** Create ranking from items in the collection, if possible */
  public Ranking toRanking(Collection<Item> items);
  
  /** Project this PreferenceSet to a new one containing only specified items. Keeps only pairs whose both items are in the specified collection */ 
  public PreferenceSet project(Collection<Item> items);
  
  /** Returns the list of items with higher preference than this one */
  public Set<Item> getHigher(Item i);
  
  /** Returns the list of items with lower preference than this one */
  public Set<Item> getLower(Item i);

  /** @return true if the set contains information that the former item is higher than the latter */
  public boolean contains(Item higher, Item lower);
  
  /** @return true if the set contains this exact preference */
  public boolean contains(Preference pref);
  
  /** @return true if the set contains information that the former item is higher than the latter */
  public boolean contains(int higherId, int lowerId);  
  
  /** @return true if contains any information about this item */
  public boolean contains(Item item);
  
  /** Removes all pairs containing this item
   * 
   * @param item to be removed
   * @return false if no pair was removed
   */
  public boolean remove(Item item);
  
  /** @return A copy of this object, with no shared data structures */
  public PreferenceSet clone();
  
  /** Set of preference pairs */
  public Set<Preference> getPreferences();
  
  /** @return Number of pairs */
  public int size();
  
  /** @return True if this preference set has no preference pairs */
  public boolean isEmpty();
  
  /** @return Set of rankings consistent with this PreferenceSet */
  public Set<Ranking> getRankings();
}
