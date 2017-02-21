package edu.drexel.cs.db.db4pref.posterior.sequential;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.Span;
import edu.drexel.cs.db.db4pref.posterior.concurrent.Expander2;
import edu.drexel.cs.db.db4pref.posterior.expander.Expander;
import edu.drexel.cs.db.db4pref.posterior.expander.State;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.TestUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/** Not any more the best sequential */
@Deprecated
public class Expander1 extends Expander {

  private Expands1 expands;
  
  private long timeout = 0; // milliseconds
  
  private double accuracy = 0;

  
  /** Creates an Expander for the given Mallows model with specified number of threads */
  public Expander1(MallowsModel model, PreferenceSet pref) {
    super(model, pref);
  }
  
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
  
  /** Set maximum allowed error from the real value */
  public void setAccuracy(double accuracy) {
    this.accuracy = accuracy;
  }
  
  /** Returns number of items that are relevant at the given step */
  private int getS(int step) {
    int s = 0;
    for (Span span: spans.values()) {
      if (step >= span.from && step <= span.to) s++;
    }
    return s;
  }
  
  public int getMaxWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w = Math.max(w, getS(i));
    }
    return w;
  }
  
  public int getSumWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w += getS(i);
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
  
  
  public Expands1 getExpands() {
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
    return expand(new State1(this));
  }
    
  @Override
  public double expand(State state) throws TimeoutException, InterruptedException {
    long start = System.currentTimeMillis();
    expands = new Expands1(this);
    expands.put((State1) state, 1d);
    Ranking reference = model.getCenter();
    if (listener != null) listener.onStart(this);
    
    
    int maxIndex = getMaxItem(pref);
    double minub1 = 1;
    double maxlb = 0;
    //double minub2 = 1;
    // Logger.info("First estimate upper / lower bound: %f, %f", Math.log(this.getUpperBound()), Math.log(this.getLowerBound()));
    for (int i = 0; i <= maxIndex; i++) {
      if (timeout > 0 && System.currentTimeMillis() - start > timeout) throw new TimeoutException("Expander timeout");
      if (listener != null) listener.onStepBegin(this, i);
      Item item = reference.get(i);
      boolean missing = !this.pref.contains(item);
      expanded += expands.states.size();
      expands = expands.insert(item, missing);
      
      if (listener != null) listener.onStepEnd(this, i);
      // printUpperBounds(expands, i);
      // if (accuracy > 0) prune(i);
      // Logger.waitKey();
      
//      if (out != null) {
//        double sump = expands.getProbability();
//        getUpperBoundSum(i);
//        getUpperBoundCount(i);
//        double ub = pruneSum ? getUpperBoundSum(i) : getUpperBoundCount(i);
//        out.println(String.format("%d,%d,%s,%d,%f,%f", id, i, pruneSum, System.currentTimeMillis() - start, Math.log(sump * ub), Math.log(sump)));
//        // Logger.info("Step %d: upper bound %f, sum p %f", i, Math.log(sump * ub), Math.log(sump));
//      }
      
      
      
      
      //double ub2 = expands.getUpperBound(item, 3);
      //minub2 = Math.min(minub2, ub2);
      //double lb1 = expands.getLowerBoundUnion();
      //Logger.info("Upper bound after item %s: %f | %f | %f | %f | %f", item, Math.log(minub1), Math.log(ub1), Math.log(minub2), Math.log(ub2), Math.log(lb1));
//      if (out != null) {
//        double ub1 = expands.getUpperBoundUnion();
//        minub1 = Math.min(minub1, ub1);
//        
//        double lb = expands.getLowerBoundUnion();
//        maxlb = Math.max(maxlb, lb);
//        
//        String line = String.format("%d,%d,%f,%f,%d", id, i+1, Math.log(maxlb), Math.log(minub1), System.currentTimeMillis() - start);
//        out.println(line);
//        out.flush();
//      }
    }
    double p = expands.getProbability();
    if (listener != null) listener.onEnd(this, p);
    return p;
  }
  
  public int removed = 0;
  public int expanded = 0;
  
  
  private class StateProb implements Comparable<StateProb> {
    final State1 state;
    final double p;
    
    StateProb(State1 state, double p) {
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
  
  public double getUpperBoundUnion(State1 state) {
    MapPreferenceSet pref = new MapPreferenceSet(tc);
    Set<Preference> prfs = state.getRanking().getPreferences();
    for (Preference pr: prfs) {
      pref.add(pr.higher, pr.lower);
    }
    double ub = model.getUpperBound(pref);
    return ub / model.z();
  }
  
  private void prune(int t) {
    if (expands.states.size() < 1000) {
      // Logger.info("Skipping step %d prune, %d states", t, expands.states.size());
      return;
    }
    
    double ub = pruneSum ? getUpperBoundSum(t) : getUpperBoundCount(t);
    double totalp = 0;
    PriorityQueue<StateProb> queue = new PriorityQueue<>();
    for (State1 state: expands.states.keySet()) {
      double p = expands.states.get(state) * ub; // getUpperBoundUnion(state); 
      queue.add(new StateProb(state, p));
      totalp += p;
    }

    double threshold = accuracy * totalp;
    double sump = 0;
    while (true) {
      StateProb sp = queue.poll();
      if (sp == null) break;
      sump += sp.p;
      if (sump < threshold) {
        removed++;
        // Logger.info("Removing step %d state with p %f", t, Math.log(sp.p));
        expands.states.remove(sp.state);
      }
      else {
        // Logger.info("Step %d, not removing state with p %f because greater than %f", t, Math.log(sp.p), Math.log(ths));
        break;
      }
    }
  }
  
    
  
  
  public void printUpperBounds(Expands1 expands, int t) {
    double ub = getUpperBoundCount(t);
    for (State1 state: expands.states.keySet()) {
      double p = expands.states.get(state);
      StringBuilder sb = new StringBuilder();
      sb.append(state).append(": ");
      sb.append(p).append(" * ").append(ub);
      sb.append(" = ").append(p * ub);
      System.out.println(sb.toString());
    }
    System.out.println();
  }
  
  /** Upper bound modifier that counts the remaining preference pairs that are inverted */
  public double getUpperBoundCount(int t) {
    int d = 0;
    for (Preference pref: tc.getPreferences()) {
      int i = referenceIndex.get(pref.higher);
      int j = referenceIndex.get(pref.lower);
      if ((i > j) && (i > t || j > t)) d++;
      
    }
    Logger.info("Count: %d", d);
    return Math.pow(model.getPhi(), d);
  }
  
  /** Upper bound modifier that sums the non-overlapping remaining preference pairs */
  public double getUpperBoundSum(int t) {
    List<Preference> prefs = new ArrayList<Preference>();
    for (Preference pref: tc.getPreferences()) {
      int i = referenceIndex.get(pref.higher);
      int j = referenceIndex.get(pref.lower);
      if ((i > j) && (i > t || j > t)) prefs.add(pref);
    }

    prefs.sort((Preference p1, Preference p2) -> {
      int i1 = referenceIndex.get(p1.higher);
      int i2 = referenceIndex.get(p2.higher);
      if (i1 < i2) return -1;
      if (i1 > i2) return 1;
      int j1 = referenceIndex.get(p1.lower);
      int j2 = referenceIndex.get(p2.lower);
      return j1 - j2;
    });
    
    int sum = 0;
    int lim = 0;
    Iterator<Preference> it = prefs.iterator();
    while (it.hasNext()) {
      Preference pref = it.next();
      int j = referenceIndex.get(pref.lower);
      // i > j
      if (j < lim) it.remove();
      else {
        int i = referenceIndex.get(pref.higher);
        sum += i - j;
        lim = i;
      }
    }
    StringBuilder sb = new StringBuilder();
    for (Preference p: prefs) {
      sb.append(p).append(" ");
    }
    Logger.info("Sum: %d (%s)", sum, sb);
    return Math.pow(model.getPhi(), sum);
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

//    {
//      long startPref = System.currentTimeMillis();
//      PreferenceExpander pex = new PreferenceExpander(model);
//      double p = pex.getProbability(pref);
//      Logger.info("PreferenceExpander: Total probability: %f in %d ms", Math.log(p), System.currentTimeMillis() - startPref);
//    }
    

    Logger.info("UB: %f", Math.log(model.getUpperBound(pref)/model.z()));

    double preal;
    {
      long startPref = System.currentTimeMillis();
      Expander2 pex = new Expander2(model, 1);
      preal = pex.getProbability(pref);
      Logger.info("Expander2: Total probability: %f in %d ms", Math.log(preal), System.currentTimeMillis() - startPref);
    }

    {
      long startPref = System.currentTimeMillis();
      Expander1 pex = new Expander1(model, pref);
      pex.setAccuracy(1E-8);
      double p = pex.expand();
      Logger.info("Expander1: Total probability: %f in %d ms", Math.log(p), System.currentTimeMillis() - startPref);
      Logger.info("Real probability: %f", Math.log(preal));
      Logger.info("Error: " + (preal - p));
    }
    
//    for (int threads = 1; threads <= Runtime.getRuntime().availableProcessors(); threads++) {
//      long startPref = System.currentTimeMillis();
//      Expander2 pex = new Expander2(model, threads);
//      double p = pex.getProbability(pref);
//      Logger.info("Expander2 x%d: Total probability: %f in %d ms", threads, Math.log(p), System.currentTimeMillis() - startPref);
//    }
  }
  
}
