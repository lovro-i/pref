package edu.drexel.cs.db.db4pref.posterior.sequential2;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.expander.Expander;
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
  
  
  public void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }
  
  /** Sets the maximum allowed width, and the step from which item removal can start */
  public void setMaxWidth(int maxWidth, int start) {
    this.maxWidth = maxWidth;
    this.startRelax = start;
  }
  
  
//  
//  public void setMaxWidth(int maxWidth, int start) {
//    int maxIndex = getMaxItem(pref);
//    List<Item> tracked;
//    for (int step = start; step <= maxIndex; step++) {
//      tracked = this.getTrackedItems(step);
//      // Logger.info("Step %d: width %d", step, tracked.size());
//      while (tracked.size() > maxWidth) {
//        Item toRemove = tracked.get(0);
//        pref.remove(toRemove);
//        // Logger.info("Removing item %s at step %d, %d remaining", toRemove, step, tracked.size() - 1);
//        this.calculateSpans();
//        tracked = this.getTrackedItems(step);
//      }
//    }
//  }

  private int maxWidth = 0;
  private int startRelax = 0;
  
  private void relax(int step) {
    if (maxWidth <= 0) return;
    if (step < startRelax) return;
    
    List<Item> tracked = this.getTrackedItems(step);
    while (tracked.size() > maxWidth) {
      Item toRemove = tracked.get(0);
      pref.remove(toRemove);
      this.calculateSpans();
      tracked = this.getTrackedItems(step);
    }
  }
  
  public static void main(String args[]) throws TimeoutException, InterruptedException {
    MapPreferenceSet pref = TestUtils.generate(20, 4, 5);
    
//    ItemSet its = new ItemSet(30);
//    its.tagOneBased();
//    MapPreferenceSet pref = new MapPreferenceSet(its);
//    pref.addByTag(24, 19);
//    pref.addByTag(26, 11);
//    pref.addByTag(25, 14);
//    pref.addByTag(25, 15);
//    pref.addByTag(22, 13);
    
//    pref.addByTag(18, 11);
//    pref.addByTag(4, 30);
//    pref.addByTag(25, 22);
//    pref.addByTag(16, 30);
//    pref.addByTag(1, 18);
    
    Logger.info(pref);
    ItemSet items = pref.getItemSet();    
    items.tagOneBased();

    double phi = 0.5;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);

    Expander1 expander1 = new Expander1(model, pref);
    System.out.println(expander1.expand());
    
    Expander2 expander2 = new Expander2(model, pref);
    System.out.println(expander2.expand());

  }
  
}
