package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.concurrent6.Expander6;
import edu.drexel.cs.db.db4pref.posterior.sequential.Expander1;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Reachable {

  private final Expander expander;

  public Reachable(Expander expander) {
    this.expander = expander;
  }
  
  /** Set of items that are between the steps, and have to be before the item */
  public Set<Item> itemsBefore(Item item, int fromStep, int toStep) {
    Set<Item> befores = new HashSet<Item>();
    for (Item it: this.expander.getTransitiveClosure().getHigher(item)) {
      int idx = this.expander.getReferenceIndex(it);
      if (idx >= fromStep && idx < toStep) befores.add(it);
    }
    return befores;
  }
  
  /** Set of items that are between the steps, and have to be after the item */
  public Set<Item> itemsAfter(Item item, int fromStep, int toStep) {
    Set<Item> afters = new HashSet<Item>();
    for (Item it: this.expander.getTransitiveClosure().getLower(item)) {
      int idx = this.expander.getReferenceIndex(it);
      if (idx >= fromStep && idx < toStep) afters.add(it);
    }
    return afters;
  }
  
  public boolean isReachable(State from, State to) {
    int stepFrom = from.length();
    int stepTo = to.length();
    if (stepFrom >= stepTo) return false;
    Item item = expander.getModel().getCenter().get(stepTo - 1);

    int betweenBefore = itemsBefore(item, stepFrom, stepTo).size();
    int betweenAfter = itemsAfter(item, stepFrom, stepTo).size();
    
    Set<Item> befores = itemsBefore(item, 0, stepFrom); // lc
    Set<Item> afters = itemsAfter(item, 0, stepFrom); // uc
    
    int pos = to.indexOf(item);
    // Logger.info("Item is at position %d", pos);
    
    int minPos = befores.size();
    // Logger.info("Initial minPos: %d (beforesSize = %d)", minPos, befores.size());
    for (Item s: befores) {
      int m = from.indexOf(s) + betweenBefore + 1;
      // Logger.info("Item %s at position %d + betweenBefore %d + 1, min is %d", s, from.indexOf(s), betweenBefore, m);
      minPos = Math.max(minPos, m);
    }
    // Logger.info("Minimum possible position is %d\n", minPos);
    if (pos < minPos) return false;

    
    int maxPos = stepTo - afters.size();
    // Logger.info("Initial maxPos:, %d (stepTo = %d, aftersSize = %d)", maxPos, stepTo, afters.size());
    for (Item s: afters) {
      int m = from.indexOf(s) + stepTo - stepFrom - betweenAfter;
      // Logger.info("Item %s at position %d + %d - %d - %d = max is %d", s, from.indexOf(s), stepTo, stepFrom, betweenAfter, m);
      maxPos = Math.min(maxPos, m);
    }
    // Logger.info("Maximum position is %d", maxPos);
    if (pos >= maxPos) return false;
    
    return true;
  }
  
  public static void main(String[] args) throws Exception {
    ItemSet items = new ItemSet(10);
    items.tagLetters();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addById(4, 1); // E > B
    v.addById(4, 3); // E > D
    v.addById(1, 2); // B > C
    System.out.println(v);
    
    Expander expander = new Expander1(model, v);
    System.out.println(expander.expand());
    
    Expander6 expander6 = new Expander6(model, 6, 3);
    System.out.println(expander6.getProbability(v));
//    
//    Expander expander = new Expander(model, v, BreathFirst.getInstance(), true);
//    
//    int[] miss1 = { 0, 0, 0 };
//    int[] its1 = { 1, 0 };
//    State from = new State(expander, its1, miss1);
//    System.out.println(from);
//    
//    
//    int[] miss2 = { 1, 3 };
//    int[] its2 = { 4 };
//    State to = new State(expander, its2, miss2);
//    // to.compact();
//    Logger.info("From state %s to state %s", from, to);
//    
//    Reachable reachable = new Reachable(expander);
//    Logger.info("Reachable: " + reachable.isReachable(from, to));
  }
}
