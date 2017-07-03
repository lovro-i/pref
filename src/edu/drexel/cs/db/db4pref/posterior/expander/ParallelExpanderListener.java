package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.posterior.concurrent2.Expander2;
import java.util.concurrent.TimeoutException;

public interface ParallelExpanderListener {

  public void onStart(Expander2 expander);

  public void onStepBegin(Expander2 expander, int step) throws TimeoutException;

  public void onStepEnd(Expander2 expander, int step);

  public void onEnd(Expander2 expander, double p);

}