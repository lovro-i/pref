package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.SparsePreferenceSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class HasseDiagram {

  private SparsePreferenceSet hasseDiagram;
  private final Set<Item> itemsInHasseDiagram = new HashSet<>();
  
  private final Map<Item, Set<Preference>> itemToPrefsInTC = new HashMap<>();

  public final PreferenceSet pref;
  public final MutablePreferenceSet tc;

  
  public HasseDiagram(PreferenceSet pref) {
    this(pref, pref.transitiveClosure());
  }

  /** Provide preference set and its TC, in case you have it already. Not to calculate it again */
  public HasseDiagram(PreferenceSet pref, MutablePreferenceSet tc) {

    this.pref = pref;
    this.tc = tc;

    this.hasseDiagram = new SparsePreferenceSet(pref.getItemSet());

    for (Item item: pref.getItems()) {
      itemToPrefsInTC.put(item, new HashSet<>());
    }
    for (Preference p: tc.getPreferences()) {
      itemToPrefsInTC.get(p.higher).add(p);
      itemToPrefsInTC.get(p.lower).add(p);
    }
  }
  
  public void add(Ranking ranking) {
    for (Item item: ranking.getItems()) {
      if (pref.contains(item)) this.add(item);
    }
  }
  
  public void add(Item item) {
    // Add new edges
    hasseDiagram.addAll(itemToPrefsInTC.get(item));
      
    // Remove edges
    SparsePreferenceSet next = new SparsePreferenceSet(hasseDiagram);
    for (Preference p: hasseDiagram) {
      for (Item inter: itemsInHasseDiagram) {
        if (tc.contains(p.higher, inter) && tc.contains(inter, p.lower)) {
          next.remove(p);
          break;
        }
      }
    }
    itemsInHasseDiagram.add(item);
    this.hasseDiagram = next;
  }
  
  public SparsePreferenceSet getPreferenceSet() {
    return hasseDiagram;
  }
  
  public int size() {
    return hasseDiagram.size();
  }
  
  @Override
  public String toString() {
    return hasseDiagram.toString();
  }
  
  
}
