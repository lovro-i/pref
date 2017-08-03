package edu.drexel.cs.db.db4pref.posterior.sequential2;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.data.PreferenceIO;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.expander.Expander;
import edu.drexel.cs.db.db4pref.posterior.expander.ExpanderListener;
import edu.drexel.cs.db.db4pref.posterior.expander.LowerBoundLast;
import edu.drexel.cs.db.db4pref.posterior.expander.State;
import edu.drexel.cs.db.db4pref.posterior.sequential.Expander1;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.TestUtils;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeoutException;

/** Best sequential so far */
public class Expander2 extends Expander {

  private Expands2 expands;
  
  private long timeout = 0; // milliseconds
  
  /** Creates an Expander for the given Mallows model with specified number of threads */
  public Expander2(MallowsModel model, PreferenceSet pref) {
    super(model, pref);
  }
  
  
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
  
  public long getTimeout() {
    return timeout;
  }
  
  public int getMaxWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w = Math.max(w, getWidth(i));
    }
    return w;
  }
  
  public int getSumWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w += getWidth(i);
    }
    return w;
  }
  

  /** Returns the index of highest sigma_i in the preference set */
  private int getMaxItem(PreferenceSet pref) {
    int i = 0;
    for (Item item: pref.getItems()) {
      i = Math.max(i, referenceIndex.get(item));
    }
    return i;
  }
  
  
  public Expands2 getExpands() {
    return expands;
  }
  
  public double getLowerBound() {
    double lb = model.getLowerBound(pref);
    double result = lb / model.z();
    return result;
  }
    
  //!! REMOVE AFTER TESTING
  public static PrintWriter out = null;
  public static long id = -1;
  public static boolean pruneSum = false;
  
  public double expand() throws TimeoutException, InterruptedException {
    return expand(new State2(this));
  }
    
  public long startExpand;
  
  @Override
  public double expand(State state) throws TimeoutException, InterruptedException {
    startExpand = System.currentTimeMillis();
    expands = new Expands2(this);
    expands.put((State2) state, 1d);
    // Ranking reference = model.getCenter();
    if (listener != null) listener.onStart(this);
    
    
    int maxIndex = getMaxItem(pref);
    for (int i = 0; i <= maxIndex; i++) {
      if (timeout > 0 && System.currentTimeMillis() - startExpand > timeout) throw new TimeoutException("Expander timeout");
      if (listener != null) listener.onStepBegin(this, i);
  
      relax(i);
      expanded += expands.states.size();
      expands = expands.insert(i);
      // Logger.info("Items tracked at step %d: %s", i+1, getTrackedItems(i));
      // Logger.info("States: %d", expands.size());
      // Logger.info("States after step %d: %d, width %d", i, expands.size(), this.getWidth(i));
      
      if (listener != null) listener.onStepEnd(this, i);
  
    }
    double p = expands.getProbability();
    if (listener != null) listener.onEnd(this, p);
    return p;
  }
  
  public int removed = 0;
  public int expanded = 0;

  public void compact() {
    this.init();
    this.expands = this.expands.compact();
  }

  
  private class StateProb implements Comparable<StateProb> {
    final State2 state;
    final double p;
    
    StateProb(State2 state, double p) {
      this.state = state;
      this.p = p;
    }

    @Override
    public int compareTo(StateProb o) {
      if (this.p < o.p) return -1;
      else if (this.p > o.p) return 1;
      else return 0;
    }
  }
  
  /** Sets the maximum allowed width, and the step from which item removal can start */
  public void setMaxWidth(int maxWidth, int start) {
    this.maxWidth = maxWidth;
    this.startRelax = start;
  }
  
    /** Sets the maximum allowed width */
  public void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  private int maxWidth = 0;
  private int startRelax = 0;
  
  private void relax(int step) {
    if (maxWidth <= 0) return;
    if (step < startRelax) return;
    
    List<Item> tracked = getTrackedItems(step);
    while (tracked.size() > maxWidth) {

      // find the earliest inserted item in tracked item list
      int min = step;
      for (Item e: tracked) {
        int rank = referenceIndex.get(e);
        if (rank < min) min = rank;
      }
      Item toRemove = model.getCenter().get(min);
      pref.remove(toRemove);
      this.calculateSpans();
      tracked = getTrackedItems(step);
    }
  }
  
  public static void main(String args[]) throws TimeoutException, InterruptedException {
    ItemSet items = new ItemSet(20);
    items.tagOneBased();
//    MapPreferenceSet pref = TestUtils.generate(20, 4, 5);
    MapPreferenceSet pref = PreferenceIO.fromString("[19>12 12>8 4>8 9>16 19>11]", items);
//    MapPreferenceSet pref = PreferenceIO.fromString("[99>69 33>66 42>27 33>99 42>46 27>66]", items);
//    MapPreferenceSet pref = PreferenceIO.fromString("[88>49 66>60 60>88 35>49 26>88 65>49 58>60 58>65 58>66 " +
//            "35>60 26>100 65>60 59>88 35>65 35>66 65>66 100>88 60>49 59>35 58>88 26>59 26>60 35>88 59>49 65>88 " +
//            "26>65 26>66 58>35 58>100 100>49 66>49 59>58 59>65 58>49 100>60]", items);


    Logger.info(pref);

    double phi = 0.8;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    
//    {
//      Expander2 expander2 = new Expander2(model, pref);
//      System.out.println(Math.log(expander2.expand()));
//    }

    // test on split_step
    for (int step = 0; step < 20; step += 1) {
      Expander2 expander2 = new Expander2(model, pref);
      expander2.setMaxWidth(1, step);
      LowerBoundListener listener = new LowerBoundListener(step);
      expander2.setListener(listener);
      System.out.printf("upper = %f, lower = %f, under step=%d \n", expander2.expand(), listener.lb, step);
    }

  }

  private static class LowerBoundListener implements ExpanderListener {

    int step;
    long timeBeforeLB, timeAfterLB;
    double lb;

    private LowerBoundListener(int step) {
      this.step = step;
    }

    @Override
    public void onStart(Expander expander) {
    }

    @Override
    public void onStepBegin(Expander expander, int step) throws TimeoutException {
      if (this.step == step) {
        this.timeBeforeLB = System.currentTimeMillis();
        Expands2 expands = ((Expander2) expander).getExpands();
        double p = expands.getProbability();
        LowerBoundLast lbb = new LowerBoundLast((Expander2) expander);
        lb = 0;
        lb += lbb.getLowerBound();
        this.timeAfterLB = System.currentTimeMillis();
      }
    }

    @Override
    public void onStepEnd(Expander expander, int step) {
    }

    @Override
    public void onEnd(Expander expander, double p) {
    }

    @Override
    public int getStep() {
      return step;
    }

    @Override
    public void setStep(int step) {
      this.step = step;
    }
  }
  
}
