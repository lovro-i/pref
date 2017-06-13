package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.Span;
import edu.drexel.cs.db.db4pref.posterior.sequential.Expander1;
import edu.drexel.cs.db.db4pref.posterior.sequential.State1;
import edu.drexel.cs.db.db4pref.posterior.sequential2.Expander2;
import edu.drexel.cs.db.db4pref.posterior.sequential2.Expands2;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.Set;


public class LowerBoundBest extends LowerBound {

  private PreferenceSet pref;
  
  public LowerBoundBest(Expander expander) {
    super(expander); 
    this.pref = expander.getPreferenceSet();
  }
  
  
  
  private Span hilo(State state, Item item) {
    if (!pref.contains(item)) return new Span(0, state.miss.length - 1);
    
    Set<Item> higher = this.expander.getTransitiveClosure().getHigher(item);
    Set<Item> lower = this.expander.getTransitiveClosure().getLower(item);
    int low = 0;
    int high = state.miss.length - 1;
    for (int i = 0; i < state.items.length; i++) {
      Item it = state.items[i];
      if (higher.contains(it)) {
        low = i + 1;
      }
      if (i < high && lower.contains(it)) {
        high = i;
      }
    }
    return new Span(low, high);
  }
  
  public boolean isTracked(Item item, int step) {
    Span span = expander.getSpan(item);
    if (span == null) return false;
    if (span.from == step && span.to == step) return false;
    // Logger.info("Item %s tracked between %d and %d (asked for %d)", item, span.from, span.to, step);
    return (step >= span.from) && (step <= span.to);
  }
  
  
  @Override
  public double getLowerBound() {
    Expands2 expands = ((Expander2) expander).getExpands();
    int from = expands.length();
    int to = expander.getMaxItem();
    for (int idx = from; idx <= to; idx++) {
      // instead of expands = expands.insert(idx);
      // now there's a similar implementation that inserts only at the last set of positions
    }
    return expands.getProbability();
  }
  
  /** Calculates lower bound for one state only */
  @Deprecated
  public double getLowerBound(State state) {
    int from = state.length();
    int to = expander.getMaxItem();
    // Logger.info("From step %d to %d\n", from, to);
    
    Ranking reference = expander.getModel().getCenter();
    double p = 1;
    State state1 = state.clone();
    for (int idx = from; idx <= to; idx++) {
      Item item = reference.get(idx);
      Span hilo = hilo(state1, item);
      // Logger.info("Hilo for item %s in state %s: %s", item, state1, hilo);
      
      
      if (isTracked(item, idx)) {
        Item[] items1 = new Item[state1.items.length + 1];
        int[] miss1 = new int[state1.miss.length + 1];
        for (int i = 0; i < items1.length; i++) {
          if (i < hilo.to) items1[i] = state1.items[i];
          else if (i == hilo.to) items1[i] = item;
          else items1[i] = state1.items[i-1];
        }
        for (int i = 0; i < miss1.length; i++) {
          if (i <= hilo.to) miss1[i] = state1.miss[i];
          else if (i == hilo.to + 1) miss1[i] = 0;
          else miss1[i] = state1.miss[i-1];
        }
        state1.items = items1;
        state1.miss = miss1;
        int pos = state1.indexOf(item);
        p *= expander.probability(idx, pos);
        // Logger.info("Inserting %d at position %d", idx, pos);
      }
      else {
        double best = 0;
        int bestIdx = -1;

        int start = 0;
        for (int i = 0; i < hilo.from; i++) {
          start += state1.miss[i] + 1;
        }
        for (int ho = hilo.from; ho <= hilo.to; ho++) {
          double sum = 0;
          for (int i = 0; i <= state1.miss[ho]; i++) {
            int pos = start + i;
            sum += expander.probability(idx, pos);
            // Logger.info("Inserting %d at position %d", idx, pos);
          }
          // Logger.info("---- sum of index %d is %f", ho, sum);
          if (sum > best) {
            best = sum;
            bestIdx = ho;
          }

          start += state1.miss[ho] + 1;
        }

        p *= best;
        state1.miss[bestIdx]++;
      }
      
      state1.compact();
      // Logger.info("State after item %s: %s\n", item, state1);
    }
    return p;
  }
  
  public static void main(String[] args) throws Exception {
    ItemSet items = new ItemSet(10);
    items.tagZeroBased();
    
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet pref = new MapPreferenceSet(items);
    pref.addById(3, 2);
    pref.addById(6, 3);
    pref.addById(3, 4);
    Logger.info(pref);
    
    
    Expander1 expander = new Expander1(model, pref);
    double p = expander.expand();
    
    int[] miss = { 1, 1, 0 };
    int[] its = {2, 3};
    State state = new State1(expander, its, miss);
    Logger.info("State: %s", state);
    Logger.info("List: %s", state.toList());
    Logger.info("TC: %s", expander.getTransitiveClosure());
    
    
    LowerBound lb = new LowerBoundBest(expander);
    double l = lb.getLowerBound();
    System.out.println(l);
  }
  
}
