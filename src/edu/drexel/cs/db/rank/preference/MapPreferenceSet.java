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
 * MapPreferenceSet is in format HashMap<Item,HashSet<Item>>. It records the
 * children of each item. It represents the preference set as a DAG.
 */
public class MapPreferenceSet extends HashMap<Item, HashSet<Item>> implements MutablePreferenceSet {

  private final ItemSet items;

  public MapPreferenceSet(ItemSet itemSet) {
    this.items = itemSet;
    for (Item e : items) {
      this.put(e, new HashSet<>());
    }
  }

  /**
   * It runs BFS to check if input edge will bring cycles. BFS starts from v to
   * u when adding preference (Item u, Item v)
   *
   * @param higher Item higher is preferred in input edge.
   * @param lower Item lower is less preferred in input edge.
   * @return true if input edge will bring cycles. Otherwise, false.
   */
  public boolean MakeGraphCyclic(Item higher, Item lower) {
    HashSet<Item> closeList = new HashSet<>();
    LinkedList<Item> openList = new LinkedList<>();
    openList.add(lower);
    while (!openList.isEmpty()) {
      Item currentItem = openList.poll();
      for (Item e : this.get(currentItem)) {
        if (!closeList.contains(e)) {
          openList.add(e);
        }
        openList.add(e);
        if (e.equals(higher)) {
          System.err.format("Error when adding (%s, %s)\n", higher, lower);
          return true;
        }
        closeList.add(currentItem);
      }
    }
    return false;
  }

  @Override
  public boolean add(Item higher, Item lower) {
    if (!MakeGraphCyclic(higher, lower)) {
      this.get(higher).add(lower);
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
    } else {
      System.err.format("Error when removing pair (%s,%s), this pair doesn't exsit.", item1, item2);
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
    return this.get(preferred).contains(over);
  }

  @Override
  public Boolean isPreferred(int preferred, int over) {
    return isPreferred(items.get(preferred), items.get(over));
  }

  @Override
  public DensePreferenceSet transitiveClosure() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public MapPreferenceSet transitiveClosureOfMap() {
    MapPreferenceSet prefsNewTC = this.deepCopy();
    MapPreferenceSet prefsOldTC;
    do {
      prefsOldTC = prefsNewTC.deepCopy();
      for (Item eParent : prefsOldTC.keySet()) {
        for (Item eChild : prefsOldTC.get(eParent)) {
          HashSet<Item> grandchildren = prefsOldTC.get(eChild);
          prefsNewTC.get(eParent).addAll(grandchildren);
        }
      }
    } while (!prefsNewTC.equals(prefsOldTC));
    return prefsNewTC;
  }

  @Override
  public Ranking project(Collection<Item> items) {
    Set<Item> constraintItems = new HashSet<>();
    constraintItems.addAll(items);

    MapPreferenceSet prefsTC = this.transitiveClosureOfMap();
    MapPreferenceSet prefsProjected = new MapPreferenceSet((ItemSet) items);

    for (Item e : prefsProjected.keySet()) {
      HashSet<Item> commonItems = new HashSet<>();
      commonItems.addAll(constraintItems);
      commonItems.retainAll(prefsTC.get(e));
      prefsProjected.put(e, commonItems);
    }

    int pairsSum = 0;
    HashMap<Integer, Item> numToItem = new HashMap<>();
    for (Item e : prefsProjected.keySet()) {
      int numChildren = prefsProjected.get(e).size();
      pairsSum += numChildren;
      numToItem.put(numChildren, e);
    }
    int keySetSize = prefsProjected.keySet().size();
    if (pairsSum == keySetSize * (keySetSize - 1) / 2) {
      Ranking r = new Ranking((ItemSet) items);
      for (int i = keySetSize - 1; i >= 0; i--) {
        r.add(numToItem.get(i));
      }
      return r;
    }
    System.err.println("No ranking can be generated from this preference set.");
    return new Ranking((ItemSet) items);
  }

  @Override
  public Set<Item> getHigher(Item i) {
    Set<Item> higherItems = new HashSet<>();
    for (Item e : this.keySet()) {
      if (this.get(e).contains(i)) {
        higherItems.add(e);
      }
    }
    return higherItems;
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
    return this.containsKey(item) || this.containsValue(item);
  }

  @Override
  public MapPreferenceSet clone() {
    MapPreferenceSet prefsClone = new MapPreferenceSet(items);
    prefsClone.putAll(this);
    return prefsClone;
  }

  public MapPreferenceSet deepCopy() {
    MapPreferenceSet copy = new MapPreferenceSet(items);
    for (Entry<Item, HashSet<Item>> entry : this.entrySet()) {
      copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }
    return copy;
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
    System.out.format("Now print the inital preference set: %s.\n", prefs.toString());
    System.out.println("Error message shows that cycle detection works when adding illegal edge.\n");
    prefs.add(c, a);
    prefs = prefs.transitiveClosureOfMap();
    System.out.println("After transitive closure:");
    System.out.println(prefs);
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
  }
}
