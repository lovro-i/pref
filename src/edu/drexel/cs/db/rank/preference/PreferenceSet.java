package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import java.util.Set;

/** Set of preferences of one user */
public interface PreferenceSet {

  
  public ItemSet getItemSet();
  
  /** Returns true if contains this preference, false if contains the opposite, and null if it doesn't contain the information on this pair */
  public Boolean isHigher(Item higher, Item lower);
  
  /** Returns true if contains this preference, false if contains the opposite, and null if it doesn't contain the information on this pair */
  public Boolean isHigher(int higher, int lower);
  
  /** Returns the transitive closure of this preference set, as DenscePreferenceSet */
  public DensePreferenceSet transitiveClosure();
  
  /** Returns the list of items with higher preference than this one */
  public Set<Item> getHigher(Item i);
  
  /** Returns the list of items with lower preference than this one */
  public Set<Item> getLower(Item i);

    
  // public void add(Item higher, Item lower);
  
  // public Sample toSample();
  
  
}
