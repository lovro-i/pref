package edu.drexel.cs.db.rank.rating;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.MapPreferenceSet;
import edu.drexel.cs.db.rank.core.MutablePreferenceSet;
import edu.drexel.cs.db.rank.core.Preference;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.core.SparsePreferenceSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class RatingSet extends HashMap<Item, Float> implements PreferenceSet {

  protected ItemSet itemSet;
  
  public RatingSet(ItemSet itemSet) {
    this.itemSet = itemSet;
  }
  
  
  @Override
  public ItemSet getItemSet() {
    return itemSet;
  }

  @Override
  public Set<Item> getItems() {
    return this.keySet();
  }

  @Override
  public Boolean isPreferred(Item preferred, Item over) {
    Float v1 = this.get(preferred);
    if (v1 == null) return null;
    
    Float v2 = this.get(over);
    if (v2 == null) return null;
    
    if (v1 == v2) return null;
    return v1 > v2;
  }

  @Override
  public Boolean isPreferred(int preferred, int over) {
    return isPreferred(itemSet.get(preferred), itemSet.get(over));
  }

  @Override
  public MapPreferenceSet transitiveClosure() {
    SparsePreferenceSet sps = new SparsePreferenceSet(this);
    return sps.transitiveClosure();
  }

  @Override
  public Ranking toRanking(Collection<Item> items) {
    SparsePreferenceSet sps = new SparsePreferenceSet(this);
    return sps.toRanking(items);
  }
  
  @Override
  public PreferenceSet project(Collection<Item> items) {
    MapPreferenceSet ps = new MapPreferenceSet(this);
    return ps.project(items);
  }

  @Override
  public Set<Item> getHigher(Item item) {
    Set<Item> higher = new HashSet<Item>();
    if (this.contains(item)) {
      float v = this.get(item);
      for (Item i: this.keySet()) {
        float v2 = this.get(i);
        if (v2 > v) higher.add(i);
      }
    }
    return higher;
  }

  @Override
  public Set<Item> getLower(Item item) {
    Set<Item> higher = new HashSet<Item>();
    if (this.contains(item)) {
      float v = this.get(item);
      for (Item i: this.keySet()) {
        float v2 = this.get(i);
        if (v2 < v) higher.add(i);
      }
    }
    return higher;
  }
  
  public Float getRating(Item item) {
    return this.get(item);
  }

  @Override
  public boolean contains(Item higher, Item lower) {
    Float v1 = this.get(higher);
    if (v1 == null) return false;
    Float v2 = this.get(lower);
    if (v2 == null) return false;
    return v1 > v2;
  }

  @Override
  public boolean contains(Preference pref) {
    return contains(pref.higher, pref.lower);
  }

  @Override
  public boolean contains(int higherId, int lowerId) {
    return contains(itemSet.get(higherId), itemSet.get(lowerId));
  }

  @Override
  public boolean contains(Item item) {
    return super.containsKey(item);
  }

  @Override
  public boolean remove(Item item) {
    return super.remove(item) != null;
  }

  @Override
  public RatingSet clone() {
    RatingSet ratings = new RatingSet(itemSet);
    ratings.putAll(this);
    return ratings;
  }

  @Override
  public Set<Preference> getPreferences() {
    Set<Preference> prefs = new HashSet<Preference>();
    for (Item item1: this.keySet()) {
      float v1 = this.get(item1);
      for (Item item2: this.keySet()) {
        float v2 = this.get(item2);
        if (v1 > v2) prefs.add(new Preference(item1, item2));
      }
    }
    return prefs;
  }

  @Override
  public int size() {
    int size = 0;
    for (Item item1: this.keySet()) {
      float v1 = this.get(item1);
      for (Item item2: this.keySet()) {
        float v2 = this.get(item2);
        if (v1 > v2) size++;
      }
    }
    return size;
  }

  @Override
  public Set<Ranking> getRankings() {
    MapPreferenceSet pref = new MapPreferenceSet(this);
    return pref.getRankings();
  }

  
}
