/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.posterior.labeled;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Partial order of item sets. Each Label in label preferences are first converted into a set of items with such label.
 * 
 * @author hping
 */
public class ItemSetPreferences {

  // a list of item sets, each set is corresponding to a label.
  private List<Set<Item>> itemSetList = new ArrayList<>();
  // label preferences where each label is denoted by it index in itemSetList.
  private Set<List<Integer>> itemSetPrefs = new HashSet<>();
  // Quickly find which set this item belongs to.
  private Map<Item, Integer> itemToSetMapping = new HashMap<>();

  public void add(Set<Item> higherSet, Set<Item> lowerSet) {

    int higherSetIdx = itemSetList.indexOf(higherSet);
    if (higherSetIdx == -1) {
      higherSetIdx = itemSetList.size();
      itemSetList.add(higherSet);
      for (Item e : higherSet) {
        itemToSetMapping.put(e, higherSetIdx);
      }
    }

    int lowerSetIdx = itemSetList.indexOf(lowerSet);
    if (lowerSetIdx == -1) {
      lowerSetIdx = itemSetList.size();
      itemSetList.add(lowerSet);
      for (Item e : lowerSet) {
        itemToSetMapping.put(e, lowerSetIdx);
      }
    }

    itemSetPrefs.add(new ArrayList<>(Arrays.asList(higherSetIdx, lowerSetIdx)));

  }

  public List<Set<Item>> getItemSetList() {
    return itemSetList;
  }

  public Set<List<Integer>> getItemSetPreferences() {
    return itemSetPrefs;
  }

  public Map<Item, Integer> getItemToSetMapping() {
    return itemToSetMapping;
  }
}
