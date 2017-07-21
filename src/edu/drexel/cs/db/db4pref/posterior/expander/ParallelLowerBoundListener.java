package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.posterior.concurrent2.Expander2;

import java.util.concurrent.TimeoutException;

public class ParallelLowerBoundListener implements ParallelExpanderListener {

  private int step;
  public long timeBeforeLB, timeAfterLB;
  public double lb;

  public ParallelLowerBoundListener(int step) {
    this.step = step;
  }

  @Override
  public int getStep() {
    return step;
  }

  @Override
  public void setStep(int step) {
    this.step = step;
  }

  @Override
  public void onStart(Expander2 expander) {
  }

  @Override
  public void onStepBegin(Expander2 expander, int step) throws TimeoutException {
    if (this.step == step) {
      this.timeBeforeLB = System.currentTimeMillis();
      Expander2 expander2 = expander.clone();
      try {
        lb = expander2.expand(true);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      this.timeAfterLB = System.currentTimeMillis();
    }
  }

  @Override
  public void onStepEnd(Expander2 expander, int step) {
  }

  @Override
  public void onEnd(Expander2 expander, double p) {
  }
}
