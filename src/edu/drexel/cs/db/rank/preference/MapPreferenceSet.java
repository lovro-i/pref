package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
<<<<<<< HEAD
 * MapPreferenceSet stores the preferences as two maps between an item and its lower / higher items. It records Directed Acyclic Graph (DAG) of
 * preferences. Each entry shows that Item e is preferred to items in HashSet<Item>. It has a reverseMap which represents the reverse preference
 * graph, where key is less preferred items and values are preferred items.
=======
 * MapPreferenceSet stores the preferences as two maps between an item and its
 * lower / higher items. It records Directed Acyclic Graph (DAG) of preferences.
 * Each entry shows that Item e is preferred to items in HashSet<Item>. It has a
 * reverseMap which represents the reverse preference graph, where key is less
 * preferred items and values are preferred items.
>>>>>>> 09f22f6c08dff1f0823c95073e33fc6e7a4ad276
 */
public class MapPreferenceSet implements MutablePreferenceSet {

  private final ItemSet items;

  /**
   * Maps an item to the set of less preferred items
   */
  private HashMap<Item, HashSet<Item>> lowers = new HashMap<>();

  /**
   * Maps an item to the set of more preferred items
   */
  private HashMap<Item, HashSet<Item>> highers = new HashMap<>();

  public MapPreferenceSet(ItemSet itemSet) {
    this.items = itemSet;
  }

  public MapPreferenceSet(PreferenceSet prefs) {
    this(prefs.getItemSet());
    for (Preference pref : prefs.getPreferences()) {
      this.add(pref.higher, pref.lower);
    }
  }

  /**
   * Runs BFS to check if input edge will bring cycles. BFS starts from lower to
   * higher when adding preference (Item higher, Item lower)
   *
   * @param higher Item higher is preferred in input edge.
   * @param lower Item lower is less preferred in input edge.
   * @return true if it is possible to add this pair, false if it would
   * introduce a cycle
   */
  private boolean checkAcyclic(Item higher, Item lower) {
    HashSet<Item> closeList = new HashSet<>();
    LinkedList<Item> openList = new LinkedList<>();
    openList.add(lower);
    while (!openList.isEmpty()) {
      Item currentItem = openList.poll();
      if (lowers.containsKey(currentItem)) {
        for (Item e : lowers.get(currentItem)) {
          if (!closeList.contains(e)) {
            openList.add(e);
          }
          if (e.equals(higher)) {
            return false;
          }
          closeList.add(currentItem);
        }
      }
    }
    return true;
  }

  @Override
  public int size() {
    int size = 0;
    for (Item i : lowers.keySet()) {
      size += lowers.get(i).size();
    }
    return size;
  }

  /**
   * Removes the empty sets
   */
  public void prune() {
    Set<Item> empty = new HashSet<Item>();
    for (Item i : lowers.keySet()) {
      if (lowers.get(i).isEmpty()) {
        empty.add(i);
      }
    }
    for (Item i : empty) {
      lowers.remove(i);
    }

    empty.clear();
    for (Item i : this.highers.keySet()) {
      if (this.highers.get(i).isEmpty()) {
        empty.add(i);
      }
    }
    for (Item i : empty) {
      this.highers.remove(i);
    }
  }

  @Override
  public boolean add(Item higher, Item lower) {
    if (!checkAcyclic(higher, lower)) {
      throw new IllegalStateException(String.format("Cannot add (%s, %s) pair, graph would be cyclic", higher, lower));
    }

    HashSet<Item> lo = lowers.get(higher);
    if (lo == null) {
      lo = new HashSet<Item>();
      lowers.put(higher, lo);
    }
    boolean added = lo.add(lower);

    HashSet<Item> hi = highers.get(lower);
    if (hi == null) {
      hi = new HashSet<Item>();
      highers.put(lower, hi);
    }
    hi.add(higher);

    return added;
  }

  @Override
  public boolean add(int higherId, int lowerId) {
    return add(items.get(higherId), items.get(lowerId));
  }

  @Override
  public Boolean remove(Item item1, Item item2) {
    Boolean preferred = this.isPreferred(item1, item2);
    if (preferred == null) {
      return null;
    }
    if (preferred) {
      lowers.get(item1).remove(item2);
      highers.get(item2).remove(item1);
    } else {
      lowers.get(item2).remove(item1);
      highers.get(item1).remove(item2);
    }
    return preferred;
  }

  @Override
  public Boolean remove(int itemId1, int itemId2) {
    return remove(items.get(itemId1), items.get(itemId2));
  }

  @Override
  public boolean remove(Preference pref) {
    boolean contains = this.contains(pref);
    if (contains) {
      lowers.get(pref.higher).remove(pref.lower);
      highers.get(pref.lower).remove(pref.higher);
    }
    return contains;
  }

  @Override
  public ItemSet getItemSet() {
    return items;
  }

  // This method didn't consider transitive closure, maybe isExplicitPreferred is better? Or should MapPreferenceSet always keep transitive closure?
  @Override
  public Boolean isPreferred(Item preferred, Item over) {
    Set<Item> lo = lowers.get(preferred);
    if (lo != null && lo.contains(over)) {
      return true;
    }

    lo = lowers.get(over);
    if (lo != null && lo.contains(preferred)) {
      return false;
    }

    return null;
  }

  @Override
  public Boolean isPreferred(int preferred, int over) {
    return isPreferred(items.get(preferred), items.get(over));
  }

  /**
<<<<<<< HEAD
   * Performs transitive closure on this preference set (not creating a new object)
=======
   * Performs transitive closure on this preference set (not creating a new
   * object)
>>>>>>> 09f22f6c08dff1f0823c95073e33fc6e7a4ad276
   */
  public void transitiveClose() {
    boolean done = false;
    List<Preference> add = new ArrayList<Preference>();
    while (!done) {
      add.clear();
      for (Item h : this.lowers.keySet()) {
        HashSet<Item> inters = lowers.get(h);
        if (inters == null) {
          continue;
        }
        for (Item inter : inters) {
          HashSet<Item> lo = lowers.get(inter);
          if (lo == null) {
            continue;
          }
          for (Item l : lo) {
            if (!this.contains(h, l)) {
              add.add(new Preference(h, l));
            }
          }
        }
      }
      for (Preference pref : add) {
        this.add(pref.higher, pref.lower);
      }
      done = add.isEmpty();
    }
  }

  @Override
  public MapPreferenceSet transitiveClosure() {
    MapPreferenceSet tc = this.clone();
    tc.transitiveClose();
    return tc;
  }

  @Override
  public Ranking project(Collection<Item> items) {
<<<<<<< HEAD

    HashSet<Item> projectedItems = new HashSet<>(items);
    // compute its transitive closure before computing projected ranking
    MapPreferenceSet prefsTC = this.transitiveClosure();
    // the projected preference pairs
    MapPreferenceSet prefsProjected = new MapPreferenceSet(this.items);

    HashSet<Item> keysProjected = new HashSet<>(projectedItems);
    keysProjected.retainAll(prefsTC.lowers.keySet());
    for (Item key : keysProjected) {
      HashSet<Item> valuesProjected = new HashSet<>(projectedItems);
      valuesProjected.retainAll(prefsTC.lowers.get(key));
      prefsProjected.lowers.put(key, valuesProjected);
    }

    int rankingLength = items.size();
    if (prefsProjected.size() < rankingLength * (rankingLength - 1) * 0.5) {
      return null;
    }

    // Map<number of descendents, item>, it shows how much this item is preferred.
    HashMap<Integer, Item> numToItem = new HashMap<>();
    Item lastItem = null;
    for (Item e : prefsProjected.lowers.keySet()) {
      int numChildren = prefsProjected.lowers.get(e).size();
      System.out.println("Item is "+e+", which has "+numChildren+" children");
      numToItem.put(numChildren, e);
      if (numChildren == 1){
        lastItem = prefsProjected.lowers.get(e).iterator().next();
          }
    }
    numToItem.put(0, lastItem);
    Ranking r = new Ranking(this.items);
    for (int i = rankingLength - 1; i >= 0; i--) {
      r.add(numToItem.get(i));
    }
    return r;

  }

//  public Ranking toIncompleteRanking() {
//
//    // Map<number of descendents, item>, it shows how much this item is preferred.
//    HashMap<Integer, HashSet<Item>> numToItem = new HashMap<>();
//    HashSet<Item> availableItems = new HashSet<>();
//    availableItems.addAll(this.keySet());
//    availableItems.addAll(this.reverseMap.keySet());
//    for (Item e : availableItems) {
//      int numChildren = 0;
//      int numAncesters = 0;
//      if (this.containsKey(e)) {
//        numChildren = this.get(e).size();
//      }
//      if (this.reverseMap.containsKey(e)) {
//        numAncesters = this.reverseMap.get(e).size();
//      }
//      int preferenceIdx = numChildren - numAncesters;
//      if (numToItem.containsKey(preferenceIdx)) {
//        numToItem.get(preferenceIdx).add(e);
//      } else {
//        HashSet<Item> tmpSet = new HashSet<>();
//        tmpSet.add(e);
//        numToItem.put(preferenceIdx, tmpSet);
//      }
//    }
//
//    Ranking r = new Ranking(this.items);
//    for (int i = items.size(); i >= -items.size(); i--) {
//      if (numToItem.containsKey(i)) {
//        for (Item e : numToItem.get(i)) {
//          r.add(e);
//        }
//      }
//    }
//    return r;
//  }
=======
    DensePreferenceSet tc = new DensePreferenceSet(this);
    return tc.project(items);
  }


>>>>>>> 09f22f6c08dff1f0823c95073e33fc6e7a4ad276
  @Override
  public Set<Item> getHigher(Item i) {
    return this.highers.get(i);
  }

  @Override
  public Set<Item> getLower(Item i) {
    return this.lowers.get(i);
  }

  @Override
  public boolean contains(Item higher, Item lower) {
    HashSet<Item> lo = this.lowers.get(higher);
    if (lo == null) {
      return false;
    }
    return lo.contains(lower);
  }

  @Override
  public boolean contains(Preference pref) {
    return contains(pref.higher, pref.lower);
  }

  @Override
  public boolean contains(int higherId, int lowerId) {
    return contains(items.get(higherId), items.get(lowerId));
  }

  @Override
  public boolean contains(Item item) {
    return this.lowers.containsKey(item) || this.highers.containsKey(item);
  }

  /**
   * Deep copy of current MapPreferenceSet instance
   *
   * @return A clone of itself
   */
  @Override
  public MapPreferenceSet clone() {
    MapPreferenceSet clone = new MapPreferenceSet(items);

    for (Item h : lowers.keySet()) {
      HashSet<Item> lo = new HashSet<Item>();
      clone.lowers.put(h, lo);
      for (Item l : lowers.get(h)) {
        lo.add(l);
      }
    }

    for (Item l : highers.keySet()) {
      HashSet<Item> hi = new HashSet<Item>();
      clone.highers.put(l, hi);
      for (Item h : highers.get(l)) {
        hi.add(h);
      }
    }

    return clone;
  }

  @Override
  public Set<Preference> getPreferences() {
    Set<Preference> prefs = new HashSet<Preference>();
    for (Item h : this.lowers.keySet()) {
      for (Item l : this.lowers.get(h)) {
        Preference p = new Preference(h, l);
        prefs.add(p);
      }
    }
    return prefs;
  }

  @Override
  public boolean remove(Item item) {
    boolean removed = false;
    if (this.lowers.containsKey(item)) {
      removed = true;
      this.lowers.remove(item);
    }

    if (this.highers.containsKey(item)) {
      removed = true;
      this.highers.remove(item);
    }

    return removed;
  }

<<<<<<< HEAD
=======
  @Override
  public String toString() {
    return this.getPreferences().toString();
  }

  @Override
  public Set<Item> getItems() {
    Set<Item> items = new HashSet<Item>();
    items.addAll(highers.keySet());
    items.addAll(lowers.keySet());
    return items;
  }

>>>>>>> 09f22f6c08dff1f0823c95073e33fc6e7a4ad276
}
