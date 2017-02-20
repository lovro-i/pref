package edu.drexel.cs.db.db4pref.posterior;

import cern.colt.Arrays;
import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.data.PreferenceIO;
import edu.drexel.cs.db.db4pref.gm.HasseDiagram;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import edu.drexel.cs.db.db4pref.util.TestUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeoutException;


public class Estimator {

  private final MallowsModel model;
  private final Ranking reference;
  private final PreferenceSet pref;
  private final PreferenceSet tc;
  private final Map<Item, Integer> referenceIndex;
 
  private Map<Item, Integer> lowest = new HashMap<Item, Integer>();
  private Map<Item, Integer> highest = new HashMap<Item, Integer>();
  
  private Map<Item, Span> spans;
  
  public Estimator(MallowsModel model, PreferenceSet pref) {
    this.model = model;
    this.reference = model.getCenter();
    this.pref = pref;
    this.tc = pref.transitiveClosure();
    this.referenceIndex = this.reference.getIndexMap();
    calculateSpans();
  }
  
  
  
  private void calculateSpans() {
    this.spans = new HashMap<Item, Span>();
    
    for (Item item: pref.getItems()) {
      int from = referenceIndex.get(item);
      Span span = new Span(from, from);
      spans.put(item, span);
    }
    
    Ranking reference = model.getCenter();
    HasseDiagram hasse = new HasseDiagram(pref);
    for (int step = 0; step < reference.length(); step++) {
      Item item = reference.get(step);
      if (pref.contains(item)) {
        hasse.add(item);
        for (Preference p: hasse.getPreferenceSet()) {
          int il = referenceIndex.get(p.lower);
          int ih = referenceIndex.get(p.higher);
          if (il < ih && ih == step) spans.get(p.lower).setTo(step);
          else if (il > ih && il == step) spans.get(p.higher).setTo(step);
        }
      }
    }
  }
 
  private Span update(int step, int known, int unknown) {
    Item item = reference.get(step);
    
    Integer from = null;
    Integer to = null;
    
    Set<Item> highs = pref.getHigher(item);
    for (Item h: highs) {
      if (referenceIndex.get(h) > step) continue;
      int mh = lowest.get(h);
      Integer mi = lowest.get(item);
      if (mi == null || from == null) from = mh + 1;
      else from = Math.min(from, mh+1);
    }
    
    Set<Item> lows = pref.getLower(item);
    for (Item l: lows) {
      if (referenceIndex.get(l) > step) continue;
      int ml = highest.get(l);
      Integer mi = highest.get(item);
      if (mi == null || to == null) to = ml + unknown - 1;
      else to = Math.max(to, ml + unknown - 1);
      Logger.info("Checking %s: %d, %d", l, to, ml + unknown -1);
    }
    
    if (from == null) from = 0;
    if (to == null) to = known + unknown;
    
    lowest.put(item, from);
    highest.put(item, to - unknown);
    return new Span(from, to);
  }
  
  /** Returns the index of highest sigma_i in the preference set */
  private int getMaxItem() {
    int i = 0;
    for (Item item: pref.getItems()) {
      i = Math.max(i, referenceIndex.get(item));
    }
    return i;
  }
  
  public long estimateStates() {
    int maxIndex = getMaxItem();
    Set<EstimatorState> states = new HashSet<EstimatorState>();
    states.add(new EstimatorState());
    
    double stateCount = 0;
    int totalUnknown = 0;
    int known = 0;
    long tStates = 0;
    for (int step = 0; step <= maxIndex; step++) {
      Item item = reference.get(step);
      if (this.pref.contains(item)) {
        Set<EstimatorState> next = new HashSet<EstimatorState>();
        for (EstimatorState es: states) {
          es.compact(step);
          next.addAll(es.insert(item));
        }
        states = next;

        known++;
        stateCount = 0;
        for (EstimatorState es: states) {
          int balls = totalUnknown + es.comps; // n
          int buckets = es.items.length + + 1; // m
          stateCount += MathUtils.ballsInBuckets(balls, buckets).doubleValue();
        }
        
      }
      else {
        totalUnknown++;
        stateCount = 0;
        for (EstimatorState es: states) {
          int balls = totalUnknown + es.comps; // n
          int buckets = es.items.length + 1; // m
          stateCount += MathUtils.ballsInBuckets(balls, buckets).doubleValue();
        }
      }
      Logger.info("EstimatorStates at item %s: %d %s", item.getTag(), states.size(), states);
      Logger.info("Estimating item %d: %d states", step + 1, (int) stateCount);
      tStates += stateCount;
    }
    return tStates;
  }
  
  public static void main(String[] args) throws TimeoutException {
    
    System.out.println(MathUtils.ballsInBuckets(2, 4).doubleValue());
    
    Random random = new Random();
    int m = 1000;
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    while (true) {
//      MapPreferenceSet v = new MapPreferenceSet(items);
//      int pairs = 10;
//      while (v.size() < pairs) {
//        Item item1 = items.get(random.nextInt(20));
//        Item item2 = items.get(random.nextInt(20));
//        if (item1.equals(item2)) continue;
//        om
//        try { v.add(item1, item2); }
//        catch (IllegalStateException e) {}
//      }
      MapPreferenceSet v = TestUtils.generate(16, 2, 4);
//      MapPreferenceSet v = new MapPreferenceSet(items);
//      v.addByTag(3, 6);
//      v.addByTag(1, 4);
//      v.addByTag(1, 5);

//      v.addByTag(5, 2);
//      v.addByTag(4, 5);
//      v.addByTag(3, 5);

      
      
      String vName = PreferenceIO.toString(v);
      Logger.info(vName);
      
      double phi = 0.8;
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);

      // EE
      PreferenceExpander expander = new PreferenceExpander(model);
      long start = System.currentTimeMillis();
      double pEE = expander.getProbability(v);
      long timeEE = System.currentTimeMillis() - start;
      Logger.info("EE done in %d ms: %f", timeEE, Math.log(pEE));

      Estimator estimator = new Estimator(model, v);
      long est = estimator.estimateStates();
      long real = expander.getSumStates();
      double err = 100d * Math.abs(real - est) / real;
        
      Logger.info("Estimated states: %d, real states: %d, error: %.1f%%", est, real, err);
      // Logger.waitKey();
      break;
    }

  }
  



  class EstimatorState {

    Item[] items;
    int comps = 0;

    EstimatorState() {
      items = new Item[0];
    }

    EstimatorState(Item[] items) {
      this.items = items;
    }
    
    EstimatorState(Item[] items, int comps) {
      this.items = items;
      this.comps = comps;
    }

    EstimatorState insert(int index, Item item) {
      Item[] novi = new Item[items.length + 1];
      for (int i = 0; i < novi.length; i++) {
        if (i == index) novi[i] = item;
        else if (i < index) novi[i] = items[i];
        else novi[i] = items[i-1];
      }
      return new EstimatorState(novi, comps);
    }

    Set<EstimatorState> insert(Item item) {
      Set<EstimatorState> states = new HashSet<EstimatorState>();
      Span span = hilo(item);
      for (int i = span.from; i <= span.to; i++) {
        states.add(this.insert(i, item));
      }
      return states;
    }

    public Span hilo(Item item) {
      Set<Item> higher = tc.getHigher(item);
      Set<Item> lower = tc.getLower(item);
      int low = 0;
      int high = items.length;
      for (int j = 0; j < items.length; j++) {
        Item it = items[j];
        if (higher.contains(it)) {
          low = j + 1;
        }
        if (lower.contains(it) && j < high) {
          high = j;
        }
      }
      return new Span(low, high);
    }

    /** Removes the items that won't figure in the future */
    void compact(int step) {
      // Logger.info("Compacting %s", this);
      for (int i = 0; i < items.length; i++) {
        Span span = spans.get(items[i]);
        if (step > span.to) {
          comps++;
          Item[] items2 = new Item[items.length-1];
          for (int j = 0; j < items2.length; j++) {
            if (j < i) items2[j] = items[j];
            else items2[j] = items[j+1];
          }

          this.items = items2;
          i--;
        }
      }
    }

    @Override
    public String toString() {
      return Arrays.toString(items) + " + " + comps;
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 79 * hash + java.util.Arrays.deepHashCode(this.items);
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final EstimatorState other = (EstimatorState) obj;
      if (this.comps != other.comps) return false;
      if (!java.util.Arrays.deepEquals(this.items, other.items)) return false;
      return true;
    }

    

    
  }
  
}