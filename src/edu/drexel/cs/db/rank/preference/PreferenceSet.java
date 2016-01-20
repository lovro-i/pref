package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Sample;

/** Set of preferences of one user */
public interface PreferenceSet {

  public void add(Item higher, Item lower);  
  
  public Sample toSample();
  
}
