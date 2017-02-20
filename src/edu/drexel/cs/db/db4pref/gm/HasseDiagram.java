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

  private SparsePreferenceSet preferences;
  private final Set<Item> items = new HashSet<Item>();
  
  private final Map<Item, Set<Preference>> prefs = new HashMap<>();
  
  public final PreferenceSet v;
  public final MutablePreferenceSet tc;

  
  public HasseDiagram(PreferenceSet v) {
    this(v, v.transitiveClosure());
  }

  /** Provide preference set and its TC, in case you have it already. Not to calculate it again */
  public HasseDiagram(PreferenceSet v, MutablePreferenceSet tc) {
    this.preferences = new SparsePreferenceSet(v.getItemSet());
    this.v = v;
    this.tc = tc;

    for (Item item: v.getItemSet()) {
      prefs.put(item, new HashSet<Preference>());
    }
    
    for (Preference pref: tc.getPreferences()) {
      prefs.get(pref.higher).add(pref);
      prefs.get(pref.lower).add(pref);
    }
  }
  
  public void add(Ranking ranking) {
    for (Item item: ranking.getItems()) {
      if (v.contains(item)) this.add(item);
    }
  }
  
  public void add(Item item) {
    // Add new edges
    preferences.addAll(prefs.get(item));
      
    // Remove edges
    SparsePreferenceSet next = new SparsePreferenceSet(preferences);      
    for (Preference p: preferences) {
      for (Item inter: items) {
        if (tc.contains(p.higher, inter) && tc.contains(inter, p.lower)) next.remove(p);
      }
    }
    items.add(item);
    this.preferences = next;
  }
  
  public SparsePreferenceSet getPreferenceSet() {
    return preferences;
  }
  
  public int size() {
    return preferences.size();
  }
  
  @Override
  public String toString() {
    return preferences.toString();
  }
  
  
}
