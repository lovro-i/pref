package edu.drexel.cs.db.db4pref.posterior.sequential;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class Expands1 {

  private final Expander1 expander;
  private Map<State1, Double> states = new HashMap<>();
  
  public Expands1(Expander1 expander) {
    this.expander = expander;
  }
  
  /** Clear this Expands so that it contains only null (empty) expansion */
  public void nullify() {
    states.clear();
    states.put(new State1(expander), 1d);
  }
  
  public void add(State1 e, double p) {
    Double prev = states.get(e);
    if (prev != null) p += prev;
    states.put(e, p);
  }
  
  public void put(State1 e, double p) {
    states.put(e, p);
  }
  
  /** Adds all the Expands to this one with weight p */
  void add(Expands1 expands, double p) {
    for (State1 e: expands.states.keySet()) {
      double v = expands.states.get(e);
      this.add(e, p * v);
    }
  }
  
  /** Returns the total probability of all the expanded states */
  public double getProbability() {
    double sum = 0;
    for (double p: states.values()) sum += p;
    return sum;
  }
  
    /** Expand possible states if the specified item is missing (can be inserted between any two present items)
   * @param item To insert
   * @return Mapping of states to their probabilities
   */
  public Expands1 insert(Item item, boolean missing) throws InterruptedException {
    Expands1 expands = new Expands1(expander);
    for (Entry<State1, Double> entry: states.entrySet()) {
      State1 state = entry.getKey();
      double p = entry.getValue();
      state.insert(expands, item, missing, p);
    }
    return expands;
  }
  
  public double getUpperBoundUnion() {
    double p = 0;
    double sump = 0;
    double maxub = 0;
    // Logger.info("Upper Bound: %d states", states.size());
    for (State1 state: states.keySet()) {
      double pState = states.get(state);
      MapPreferenceSet pref = new MapPreferenceSet(expander.pref);
      Set<Preference> prfs = state.getRanking().getPreferences();
      for (Preference pr: prfs) {
        pref.add(pr.higher, pr.lower);
      }
      
      double ub = expander.model.getUpperBound(pref);
      p += pState * ub;
      sump += pState;
      maxub = Math.max(maxub, ub);
      // if (states.size() == 1) 
      // Logger.info("    State: %s", state);
      // else Logger.info("States: %d", states.size());
    }
    // return sump;
    return maxub / expander.model.z();
  }
  
  public double getLowerBoundUnion() {
    double p = 0;
    double sump = 0;
    double minlb = 0;
    // Logger.info("Upper Bound: %d states", states.size());
    for (State1 state: states.keySet()) {
      double pState = states.get(state);
      MapPreferenceSet pref = new MapPreferenceSet(expander.pref);
      Set<Preference> prfs = state.getRanking().getPreferences();
      for (Preference pr: prfs) {
        pref.add(pr.higher, pr.lower);
      }
      
      double lb = expander.model.getLowerBound(pref);
      p += pState * lb;
      sump += pState;
      minlb = Math.min(minlb, lb);
      // if (states.size() == 1) 
      // Logger.info("    State: %s", state);
      // else Logger.info("States: %d", states.size());
    }
    // return sump;
    return minlb / expander.model.z();
  }
  
  @Deprecated
  public double getUpperBound1(Item item) {
    double p = 0;
    double z = expander.model.z();
    int step = expander.referenceIndex.get(item);
    
    MapPreferenceSet pref = new MapPreferenceSet(expander.pref.getItemSet());
    for (Preference pr: expander.pref.getPreferences()) {
      int i1 = expander.referenceIndex.get(pr.lower);
      int i2 = expander.referenceIndex.get(pr.higher);
      if (i1 > step || i2 > step) {
        pref.add(pr.higher, pr.lower);
      }
    }
    double ub = expander.model.getUpperBound(pref);
    
    for (State1 state: states.keySet()) {
      p += states.get(state);
    }
    // Logger.info("After inserting item %s: Preference set %s", item, pref);
    // Logger.info("Total states log probability: %f, Upper bound: %f, Z: %f, log(ub / z): %f, log(p * ub / z): %f", Math.log(p), ub, z, Math.log(ub / z), Math.log(p * ub / z));
    
    double result = p * ub / z;
    Logger.info("Upper bound: %f", Math.log(result));
    return result;
  }
  
  public double getUpperBound(Item item, int ver) {
    double z = expander.model.z();
    int step = expander.referenceIndex.get(item);    
    
    MapPreferenceSet pref = new MapPreferenceSet(expander.pref.getItemSet());
    for (Preference pr: expander.pref.getPreferences()) {
      int i1 = expander.referenceIndex.get(pr.lower);
      int i2 = expander.referenceIndex.get(pr.higher);
      if (i1 > step || i2 > step) {
        pref.add(pr.higher, pr.lower);
      }
    }
    double ub = getUpperBound(expander.model, pref, step+1, ver);
    
    double p = 0;
    for (State1 state: states.keySet()) {
      p += states.get(state);
    }
    // Logger.info("After inserting item %s: Preference set %s", item, pref);
    // Logger.info("Total states log probability: %f, Upper bound: %f, Z: %f, log(ub / z): %f, log(p * ub / z): %f", Math.log(p), ub, z, Math.log(ub / z), Math.log(p * ub / z));
    
    double result = ub / z;
    //Logger.info("Upper bound %d after step %d: %f", ver, step, Math.log(result));
    return result;
  }
  

  
  
  public double getUpperBound(MallowsModel model, PreferenceSet pref, int step, int ver) {
    MutablePreferenceSet tcPref = pref.transitiveClosure();
    int d = 0;
    int s = 0;
    int less = 0;
    Set<Item> lesser = new HashSet<Item>();
    for (Preference p: tcPref.getPreferences()) {
      if (model.getCenter().contains(p)) s++;
      else d++;
      int i1 = expander.referenceIndex.get(p.lower);
      int i2 = expander.referenceIndex.get(p.higher);
      if (i1 < step) lesser.add(p.lower);
      if (i2 < step) lesser.add(p.higher);
    }
    
    double phi = model.getPhi();
    
    int remaining = model.getCenter().length();
    if (ver > 1) remaining = remaining - step;
    else if (ver == 3) remaining += lesser.size();
    int size = remaining * (remaining - 1) / 2;
    
    double ub = Math.pow(phi, d) * Math.pow((1 + phi), size - s - d);
    return ub;
  }
  
  
}
