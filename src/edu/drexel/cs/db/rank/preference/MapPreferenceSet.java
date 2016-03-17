package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * MapPreferenceSet is in format HashMap<Item,HashSet<Item>>. It records
 * adjacent children for each item. Each entry means that Item e is preferred to
 * all items in HashSet<Item>. It represents the preference set as a DAG.
 * MapPreferenceSet.reverseMap represents the reverse preference graph, where
 * key is less preferred items and values are preferred items.
 */
public class MapPreferenceSet extends HashMap<Item, HashSet<Item>> implements MutablePreferenceSet {

  private final ItemSet items;
  public HashMap<Item, HashSet<Item>> reverseMap = new HashMap<>(); // reverseMap<lessPreferredItem, HashSet<preferredItems>>;

  public MapPreferenceSet(ItemSet itemSet) {
    this.items = itemSet;
  }

  /**
   * It runs BFS to check if input edge will bring cycles. BFS starts from lower
   * to higher when adding preference (Item higher, Item lower)
   *
   * @param higher Item higher is preferred in input edge.
   * @param lower Item lower is less preferred in input edge.
   */
  private void makeGraphCyclic(Item higher, Item lower) {
    HashSet<Item> closeList = new HashSet<>();
    LinkedList<Item> openList = new LinkedList<>();
    openList.add(lower);
    while (!openList.isEmpty()) {
      Item currentItem = openList.poll();
      if (this.containsKey(currentItem)) {
        for (Item e : this.get(currentItem)) {
          if (!closeList.contains(e)) {
            openList.add(e);
          }
          if (e.equals(higher)) {
            String errorMessage = String.format("Adding preference pair (%s, %s) is ignored to avoid preference graph cycles.", higher, lower);
            throw new ArithmeticException(errorMessage);
          }
          closeList.add(currentItem);
        }
      }
    }
  }

  @Override
  public boolean add(Item higher, Item lower) {
    try {
      makeGraphCyclic(higher, lower);

      if (this.containsKey(higher)) {
        this.get(higher).add(lower);
      } else {
        HashSet<Item> tmpSet = new HashSet<>();
        tmpSet.add(lower);
        this.put(higher, tmpSet);
      }

      if (this.reverseMap.containsKey(lower)) {
        this.reverseMap.get(lower).add(higher);
      } else {
        HashSet<Item> tmpSet = new HashSet<>();
        tmpSet.add(higher);
        this.reverseMap.put(lower, tmpSet);
      }
    } catch (ArithmeticException e) {
      System.err.println(e);
      return false;
    }
    return true;
  }

  @Override
  public boolean add(int higherId, int lowerId) {
    return add(items.get(higherId), items.get(lowerId));
  }

  @Override
  public Boolean remove(Item item1, Item item2) {
    boolean item1Preferred = this.get(item1).contains(item2);
    if (item1Preferred) {
      this.get(item1).remove(item2);
      if (this.get(item1).isEmpty()) {
        this.remove(item1);
      }
      this.reverseMap.get(item2).remove(item1);
      if (this.reverseMap.isEmpty()) {
        this.reverseMap.remove(item2);
      }
    } else {
      String errorMessage = String.format("Error when removing pair (%s,%s), this pair doesn't exsit.", item1, item2);
      throw new ArithmeticException(errorMessage);
    }
    return item1Preferred;
  }

  @Override
  public Boolean remove(int itemId1, int itemId2) {
    return remove(items.get(itemId1), items.get(itemId2));
  }

  @Override
  public ItemSet getItemSet() {
    return items;
  }

  @Override
  public Boolean isPreferred(Item preferred, Item over) {
    boolean preferredIsInKeySet = this.containsKey(preferred);
    boolean overIsInKeySet = this.reverseMap.containsKey(over);

    if (preferredIsInKeySet && this.get(preferred).contains(over)) {
      return true;
    } else if (overIsInKeySet && this.get(preferred).contains(over)) {
      return false;
    } else {
      return null;
    }
  }

  @Override
  public Boolean isPreferred(int preferred, int over) {
    return isPreferred(items.get(preferred), items.get(over));
  }

  @Override
  public DensePreferenceSet transitiveClosure() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public MapPreferenceSet tempTransitiveClosure() {
    MapPreferenceSet prefsNewTC = this.deepCopy();
    MapPreferenceSet prefsOldTC;
    do {
      prefsOldTC = prefsNewTC.deepCopy();
      // update original map
      for (Item eParent : prefsOldTC.keySet()) {
        for (Item eChild : prefsOldTC.get(eParent)) {
          if (prefsOldTC.containsKey(eChild)) {
            HashSet<Item> eGrandchildren = prefsOldTC.get(eChild);
            prefsNewTC.get(eParent).addAll(eGrandchildren);
          }
        }
      }
      // update reverse map
      for (Item eParent : prefsOldTC.reverseMap.keySet()) {
        for (Item eChild : prefsOldTC.reverseMap.get(eParent)) {
          if (prefsOldTC.reverseMap.containsKey(eChild)) {
            HashSet<Item> eGrandchildren = prefsOldTC.reverseMap.get(eChild);
            prefsNewTC.reverseMap.get(eParent).addAll(eGrandchildren);
          }
        }
      }
    } while (!prefsNewTC.equals(prefsOldTC));
    return prefsNewTC;
  }

  @Override
  public Ranking project(Collection<Item> items) {
    HashSet<Item> projectedItems = new HashSet<>(items);
    // compute its transitive closure before computing projected ranking
    MapPreferenceSet prefsTC = this.tempTransitiveClosure();
    // the projected preference pairs
    MapPreferenceSet prefsProjected = new MapPreferenceSet(this.items);

    HashSet<Item> commonKeyItems = new HashSet<>(projectedItems);
    commonKeyItems.retainAll(this.keySet());
    for (Item e : commonKeyItems) {
      HashSet<Item> commonItems = new HashSet<>(projectedItems);
      commonItems.retainAll(prefsTC.get(e));
      prefsProjected.put(e, commonItems);
    }

    int pairsSum = 0;
    // Map<number of descendents, item>, it shows how much this item is preferred.
    HashMap<Integer, Item> numToItem = new HashMap<>();
    for (Item e : prefsProjected.keySet()) {
      int numChildren = prefsProjected.get(e).size();
      pairsSum += numChildren;
      numToItem.put(numChildren, e);
    }
    int keySetSize = prefsProjected.keySet().size();
    if (pairsSum == keySetSize * (keySetSize - 1) / 2) {
      Ranking r = new Ranking(this.items);
      for (int i = keySetSize - 1; i >= 0; i--) {
        r.add(numToItem.get(i));
      }
      return r;
    }
    throw new ArithmeticException("No ranking can be generated from this preference set.");
  }

  public Ranking toIncompleteRanking() {

    // Map<number of descendents, item>, it shows how much this item is preferred.
    HashMap<Integer, HashSet<Item>> numToItem = new HashMap<>();
    HashSet<Item> availableItems = new HashSet<>();
    availableItems.addAll(this.keySet());
    availableItems.addAll(this.reverseMap.keySet());
    for (Item e : availableItems) {
      int numChildren = 0;
      int numAncesters = 0;
      if (this.containsKey(e)) {
        numChildren = this.get(e).size();
      }
      if (this.reverseMap.containsKey(e)){
        numAncesters = this.reverseMap.get(e).size();
      }
      int preferenceIdx = numChildren-numAncesters;
      if (numToItem.containsKey(preferenceIdx)) {
        numToItem.get(preferenceIdx).add(e);
      } else {
        HashSet<Item> tmpSet = new HashSet<>();
        tmpSet.add(e);
        numToItem.put(preferenceIdx, tmpSet);
      }
    }

    Ranking r = new Ranking(this.items);
    for (int i = items.size(); i >= -items.size(); i--) {
      if (numToItem.containsKey(i)) {
        for (Item e : numToItem.get(i)) {
          r.add(e);
        }
      }
    }
    return r;
  }

  @Override
  public Set<Item> getHigher(Item i) {
    return this.reverseMap.get(i);
  }

  @Override
  public Set<Item> getLower(Item i) {
    return this.get(i);
  }

  @Override
  public boolean contains(Item higher, Item lower) {
    return this.get(higher).contains(lower);
  }

  @Override
  public boolean contains(int higherId, int lowerId) {
    return contains(items.get(higherId), items.get(lowerId));
  }

  @Override
  public boolean contains(Item item) {
    return this.containsKey(item) || this.reverseMap.containsKey(item);
  }

  /**
   * Shallow prefsDeepCopy of current MapPreferenceSet instance
   *
   * @return a shallow prefsDeepCopy of itself
   */
  @Override
  public MapPreferenceSet clone() {
    MapPreferenceSet prefsClone = new MapPreferenceSet(items);
    prefsClone.putAll(this);
    prefsClone.reverseMap.putAll(this.reverseMap);
    return prefsClone;
  }

  /**
   * Deep prefsDeepCopy of current MapPreferenceSet instance
   *
   * @return a deep prefsDeepCopy of itself
   */
  public MapPreferenceSet deepCopy() {
    MapPreferenceSet prefsDeepCopy = new MapPreferenceSet(items);
    for (Entry<Item, HashSet<Item>> entry : this.entrySet()) {
      prefsDeepCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }
    for (Entry<Item, HashSet<Item>> entry : this.reverseMap.entrySet()) {
      prefsDeepCopy.reverseMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }

    return prefsDeepCopy;
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(4);
    MapPreferenceSet prefs = new MapPreferenceSet(items);
    Item a = items.get(0);
    Item b = items.get(1);
    Item c = items.get(2);
    Item d = items.get(3);
    prefs.add(a, b);
    prefs.add(b, c);
    prefs.add(c, d);
    System.out.format("Now print the inital preference set: %s.\n", prefs);
    System.out.format("Now print the reverse preference set: %s.\n", prefs.reverseMap);
    System.out.println("Error message shows that cycle detection works when adding illegal edge.");
    prefs.add(c, a);
    prefs = prefs.tempTransitiveClosure();
    System.out.println("After transitive closure:");
    System.out.format("Now print the inital preference set: %s.\n", prefs);
    System.out.format("Now print the reverse preference set: %s.\n", prefs.reverseMap);
    System.out.println("After project by {0,1,2}:");
    ItemSet itemsMask = new ItemSet(3);
    Ranking r = prefs.project(itemsMask);
    System.out.println(r);
    System.out.println("Test getHigher:");
    System.out.println(prefs.getHigher(d));
    System.out.println("Test isPreferred:");
    System.out.println(prefs.isPreferred(0, 1));
    System.out.println("Test remove");
    prefs.remove(1, 2);
    System.out.println(prefs);

    MapPreferenceSet p = new MapPreferenceSet(items);
    p.add(a, b);
    System.out.println(p.toIncompleteRanking());
    System.out.println(p.contains(c));
    System.out.println(p.isPreferred(d, c));
    //!! These are good examples why we need unit testing (jUnit)
  }
}
