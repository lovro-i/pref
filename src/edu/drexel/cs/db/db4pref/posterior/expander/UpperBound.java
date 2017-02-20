package edu.drexel.cs.db.db4pref.posterior.expander;


public abstract class UpperBound {

  protected final Expander expander;
  
  public UpperBound(Expander expander) {
    this.expander = expander;
  }
  
  public abstract double getUpperBoundModifier(int step);
}
