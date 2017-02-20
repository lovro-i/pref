package edu.drexel.cs.db.db4pref.posterior.expander;


public interface ExpanderListener {

  public void onStart(Expander expander);
  
  public void onStepBegin(Expander expander, int step);

  public void onStepEnd(Expander expnader, int step);

  public void onEnd(Expander expander, double p);

}
