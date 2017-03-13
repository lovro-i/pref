/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.posterior.labeled;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hping
 */
public class Expander {

  protected final MallowsModel model;
  protected Map<Item, Integer> referenceIndex; // Item -> Position in center ranking

  protected final List<Item> topMatchingItems;
  protected final Ranking initialRanking; // essentially a ranking.
  protected final Set<Item> presentItems;
  protected final ItemsetPreferences itemSetPreferences;
  protected final Map<Integer, Integer> latestParents;
  protected final LabelRanges labelRanges;

  private Expands expands;

  public Expander(MallowsModel model, Ranking initialRanking, ItemsetPreferences itemSetPrefs,
          List<Item> topMatchingItems, Map<Integer, Integer> latestParents, LabelRanges labelRanges) {
    this.model = model;
    this.initialRanking = initialRanking;
    this.presentItems = new HashSet<>(initialRanking.getItems());
    this.referenceIndex = model.getCenter().getIndexMap();

    this.itemSetPreferences = itemSetPrefs;
    this.topMatchingItems = topMatchingItems;
    this.latestParents = latestParents;
    this.labelRanges = labelRanges;
  }

  public double expand() {
    return expand(new State(this));
  }

  public double expand(State state) {
    expands = new Expands(this);
    expands.put(state, 1d);
    
    // TODO The iteration should stop when all labeled items are inserted.
    for (int i = 0; i < model.getCenter().length(); i++) {
      expands = expands.insert(i);
    }

    return expands.getProbability();
  }

  public MallowsModel getModel() {
    return model;
  }

  public Ranking getInitialRanking() {
    return initialRanking;
  }

  public Set<Item> getPresentItems() {
    return presentItems;
  }

  public int getReferenceIndex(Item e) {
    return referenceIndex.get(e);
  }

  public boolean isInPreferences(Item e) {
    return itemSetPreferences.getItemToSetMapping().keySet().contains(e);
  }

  public int getItemLabelNumber(Item e) {
    return itemSetPreferences.getItemToSetMapping().get(e);
  }

  public int getTopMatchingPositionInInitialRanking(Item e) {
    int labelNumber = itemSetPreferences.getItemToSetMapping().get(e);
    Item topMatchingItem = topMatchingItems.get(labelNumber);
    return initialRanking.indexOf(topMatchingItem);
  }

  public boolean hasLatestParent(Item e) {
    int labelNumber = itemSetPreferences.getItemToSetMapping().get(e);
    return latestParents.containsKey(labelNumber);
  }

  public int getLatestParentPositionInInitialRanking(Item e) {
    int labelNumber = itemSetPreferences.getItemToSetMapping().get(e);
    int latestParent = latestParents.get(labelNumber);
    Item latestParentItem = topMatchingItems.get(latestParent);
    return initialRanking.indexOf(latestParentItem);
  }
}
