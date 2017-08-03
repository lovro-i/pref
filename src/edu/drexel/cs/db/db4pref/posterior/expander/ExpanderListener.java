package edu.drexel.cs.db.db4pref.posterior.expander;


import java.util.concurrent.TimeoutException;

public interface ExpanderListener {

  public void onStart(Expander expander);
  
  public void onStepBegin(Expander expander, int step) throws TimeoutException;

  public void onStepEnd(Expander expander, int step);

  public void onEnd(Expander expander, double p);

  public int getStep();

  public void setStep(int step);

}
