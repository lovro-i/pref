package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.SparsePreferenceSet;
import java.util.HashSet;
import java.util.Set;


public class HasseDiagram {

  private SparsePreferenceSet preferences;
  private Set<Item> items = new HashSet<Item>();
  
  
  public PreferenceSet v;
  public MutablePreferenceSet tc;
  
  public HasseDiagram(PreferenceSet v) {
    this.preferences = new SparsePreferenceSet(v.getItemSet());
    this.v = v;
    this.tc = v.transitiveClosure();
  }
  
  
  public void add(Item item) {
    // Add new edges
    items.add(item);
    for (Preference pref: tc.getPreferences()) {
      if (items.contains(pref.higher) && items.contains(pref.lower)) preferences.add(pref);
    }
      
    // Remove edges
    SparsePreferenceSet next = new SparsePreferenceSet(preferences);      
    for (Preference p: preferences) {
      for (Item inter: items) {
        if (tc.contains(p.higher, inter) && tc.contains(inter, p.lower)) next.remove(p);
      }
    }
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
