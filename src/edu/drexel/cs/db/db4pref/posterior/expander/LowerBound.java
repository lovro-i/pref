package edu.drexel.cs.db.db4pref.posterior.expander;


import java.util.concurrent.TimeoutException;

public abstract class LowerBound {

  protected final Expander expander;

  
  public LowerBound(Expander expander) {
    this.expander = expander;
  }
  
  public abstract double getLowerBound() throws TimeoutException;
  
}
