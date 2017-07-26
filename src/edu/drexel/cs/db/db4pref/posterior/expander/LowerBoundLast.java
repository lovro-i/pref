package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.data.PreferenceIO;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.Span;
import edu.drexel.cs.db.db4pref.posterior.sequential2.Doubler;
import edu.drexel.cs.db.db4pref.posterior.sequential2.Expander2;
import edu.drexel.cs.db.db4pref.posterior.sequential2.Expands2;
import edu.drexel.cs.db.db4pref.posterior.sequential2.State2;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;


public class LowerBoundLast {

  protected final Expander2 expander;
  private PreferenceSet pref;
  
  public LowerBoundLast(Expander2 expander) {
    this.expander = expander;
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
    return (step > span.from) && (step <= span.to);
  }
    

  public double getLowerBound() throws TimeoutException {
    Expands2 expands = expander.getExpands().copy();
    int from = expands.length();
    int to = expander.getMaxItem();
    Ranking reference = expander.getModel().getCenter();
    for (int idx = from; idx <= to; idx++) {
      // instead of expands = expands.insert(idx);
      // now there's a similar implementation that inserts only at the last set of positions
      Item item = reference.get(idx);
      boolean missing = !expander.getPreferenceSet().contains(item);
      long startExpand = expander.startExpand;
      long timeout = expander.getTimeout();

      Expands2 expandsTemp = new Expands2(expander);
      for (Map.Entry<State2, Doubler> entry: expands.getStates().entrySet()) {
        if (timeout > 0 && System.currentTimeMillis() - startExpand > timeout) throw new TimeoutException("Expander timeout");
        State2 state = entry.getKey();
        double p = entry.getValue().get();
        if (missing){
          State2 stateTemp = state.clone();
          stateTemp.miss[stateTemp.miss.length-1]++;

          double prob = 0;
          for (int j = 0; j <= state.miss[state.miss.length-1]; j++) {
            prob += expander.probability(idx, idx-j);
          }
          stateTemp.compact();
          expandsTemp.add(stateTemp, prob * p);
        } else {
          // public void insertPresent(Expands2 expands, Item item, double p1) {
          Span track = expander.getSpan(item);
          Span hilo = hilo(state, item);
          if (track.from == track.to) {
            state.insertOneMissing(expandsTemp, idx, hilo.to, p);
          } else {
            state.insertOne(expandsTemp, item, hilo.to, p);
          }
        }
      }
      expands = expandsTemp;
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
        int start = 0;
        for (int i = 0; i < hilo.to; i++) {
          start += state1.miss[i] + 1;
        }

        double sum = 0;
        for (int i = 0; i <= state1.miss[hilo.to]; i++) {
          int pos = start + i;
          sum += expander.probability(idx, pos);
          // Logger.info("Inserting %d at position %d", idx, pos);
        }

        p *= sum;
        state1.miss[hilo.to]++;
      }
      state1.compact();
      // Logger.info("State after item %s: %s\n", item, state1);
      
    }
    return p;
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
        LowerBoundLast lbb = new LowerBoundLast((Expander2) expander);
        lb = lbb.getLowerBound();
        this.timeAfterLB = System.currentTimeMillis();
      }
    }

    @Override
    public void onStepEnd(Expander expander, int step) {
    }

    @Override
    public void onEnd(Expander expander, double p) {
    }
  };
  
  public static void main(String[] args) throws Exception {
    ItemSet items = new ItemSet(100);
    items.tagOneBased();
    
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
//    MapPreferenceSet pref = new MapPreferenceSet(items);
//    pref.addById(3, 2);[25>100 1>77 43>25 63>33 43>13 63>66 25>13 13>100 73>1]
//    pref.addById(6, 3);
//    pref.addById(3, 4);
//    pref.addById(7, 6);
//    Logger.info(pref);
    String prefString10 = "[25>100 1>77 43>25 63>33 43>13 63>66 25>13 13>100 73>1]";
    String prefString = "[4>7 7>9]";
    MapPreferenceSet pref = PreferenceIO.fromString(prefString10, items);

    LowerBoundListener listener = new LowerBoundListener(8);
    Expander2 expander = new Expander2(model, pref);
    expander.setListener(listener);
    long startTime = System.currentTimeMillis();
    double p = expander.expand();
    long endTime = System.currentTimeMillis();

    long timeStep = listener.timeBeforeLB - startTime;
    long timeLB = listener.timeAfterLB - listener.timeBeforeLB;
    long timeRelax = endTime - listener.timeAfterLB;
    long timeTotal = endTime - startTime;

    System.out.printf("timeStep = %d \ntimeLB = %d \ntimeRelax = %d \np = %f, lb = %f",
            timeStep, timeLB, timeRelax, p, listener.lb);
    
//    int[] miss = { 1, 1, 0 };
//    int[] its = {2, 3};
//    State state = new State2(expander, its, miss);
//    Logger.info("State: %s", state);
//    Logger.info("List: %s", state.toList());
//    Logger.info("TC: %s", expander.getTransitiveClosure());
//
//
//
//    LowerBoundLast lb = new LowerBoundLast(expander);
//    double l = lb.getLowerBound();
//    System.out.println(l);
//    System.out.println(p);
  }
  
}
