package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.core.PreferenceSet;


public abstract class LowerBound {

  protected final Expander expander;

  
  public LowerBound(Expander expander) {
    this.expander = expander;
  }
  
  public abstract double getLowerBound(State state);
  
}
