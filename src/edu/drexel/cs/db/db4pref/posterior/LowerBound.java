package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class LowerBound {

  private final MallowsModel model;
  private final Map<Item, Integer> referenceIndex;

  /** Number of missing elements at each position */
  private int[] miss;
  
  /** Array of known items */
  private Item[] items;
  
  private double p;
  
  
  public LowerBound(MallowsModel model) {
    this.model = model;
    this.referenceIndex = model.getCenter().getIndexMap();
  }
  

  /** Returns the index of highest sigma_i in the preference set */
  private int getMaxItem(PreferenceSet pref) {
    int i = 0;
    for (Item item: pref.getItems()) {
      i = Math.max(i, referenceIndex.get(item));
    }
    return i;
  }
  
  public double getLowerBound(PreferenceSet pref) {
    MutablePreferenceSet tc = pref.transitiveClosure();
    this.miss = new int[1];
    this.items = new Item[0];
    this.p = 1;
    int maxIndex = getMaxItem(pref);
    Ranking reference = model.getCenter();
    
    for (int i = 0; i <= maxIndex; i++) {
      Item item = reference.get(i);
      
      boolean missing = !pref.contains(item);
      if (missing) this.insertMissing(tc, item);
      else this.insert(tc, item);
    }
    return p;
  }
  
  public void insertMissing(MutablePreferenceSet tc, Item item) {
    miss[miss.length-1]++;
  }

  
  private HashMap<Span, Double> probs = new HashMap<>();
  
  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  double probability(int itemIndex, int position) {
    Span span = new Span(itemIndex, position);
    Double p = probs.get(span);
    if (p != null) return p;
    
    double phi = model.getPhi();
    double p1 = Math.pow(phi, Math.abs(itemIndex - position)) * (1 - phi) / (1 - Math.pow(phi, itemIndex + 1));
    probs.put(span, p1);
    return p1;
  }
  
  public void insert(MutablePreferenceSet tc, Item item) {
    Span hilo = hilo(tc, item);
    int index = hilo.to;
    
    int posPrev = index - 1;
    for (int i = 0; i < index; i++) {
      posPrev += miss[i];      
    }
    
    // create new array of items, by inserting it after the previous
    Item[] items1 = new Item[items.length + 1];
    for (int i = 0; i < items1.length; i++) {
      if (i < index) items1[i] = items[i];
      else if (i == index) items1[i] = item;
      else items1[i] = items[i - 1];
    }
    
    // create n new expand states with their probabilities    
    int[] miss1 = new int[miss.length + 1];
    int i = this.miss[index];
    //  State2 state = new State2(expander, items1);
    for (int j = 0; j < miss1.length; j++) {
      if (j < index) miss1[j] = this.miss[j];
      else if (j == index) miss1[j] = i ;
      else if (j == index + 1) miss1[j] = this.miss[index] - i;
      else miss1[j] = this.miss[j-1];        
    }
    
    p *= probability(referenceIndex.get(item), posPrev + 1 + i);
    this.miss = miss1;
    this.items = items1;
  }
  
  public Span hilo(MutablePreferenceSet tc, Item item) {
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
  
}
