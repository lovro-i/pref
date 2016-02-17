package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import java.util.Collection;
import java.util.Set;

/** Set of preferences of one user */
public interface PreferenceSet extends Cloneable {

  
  public ItemSet getItemSet();
  
  /** Returns true if contains this preference, false if contains the opposite, and null if it doesn't contain the information on this pair */
  public Boolean isHigher(Item higher, Item lower);
  
  /** Returns true if contains preference with this item IDs, false if contains the opposite, and null if it doesn't contain the information on this pair */
  public Boolean isHigher(int higher, int lower);
  
  /** Returns the transitive closure of this preference set, as DensePreferenceSet */
  public DensePreferenceSet transitiveClosure();
  
  /** Create ranking from items in the collection, if possible */
  public Ranking project(Collection<Item> items);
  
  /** Returns the list of items with higher preference than this one */
  public Set<Item> getHigher(Item i);
  
  /** Returns the list of items with lower preference than this one */
  public Set<Item> getLower(Item i);

  /** @return true if the set contains information that the former item is higher than the latter */
  public boolean contains(Item higher, Item lower);
  
  /** @return true if the set contains information that the former item is higher than the latter */
  public boolean contains(int higherId, int lowerId);  
  
  public PreferenceSet clone();
}
