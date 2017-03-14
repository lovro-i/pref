/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.posterior.labeled;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.distance.KendallTauDistance;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hping
 */
public class NaiveLabeledRIM {

  protected final MallowsModel model;

  public NaiveLabeledRIM(MallowsModel model) {
    this.model = model;
  }

  public double evaluateLabelQuery(ItemsetPreferences itemsetPrefs, LabelRanges labelRanges) {

    KendallTauDistance dist = new KendallTauDistance();
    double p = 0;
    for (List<Item> list : Collections2.orderedPermutations(model.getItemSet())) {
      for (List<Item> topMatchingItems : Sets.cartesianProduct(itemsetPrefs.getItemSetList())) {
        boolean legalForCurrentTopMatchingItem = true;
        for (List<Integer> pair : itemsetPrefs.getItemSetPreferences()) {
          Item higherItem = topMatchingItems.get(pair.get(0));
          Item lowerItem = topMatchingItems.get(pair.get(1));

          int higherItemIndex = list.indexOf(higherItem);
          int lowerItemIndex = list.indexOf(lowerItem);

          if (higherItemIndex > lowerItemIndex) {
            legalForCurrentTopMatchingItem = false;
            break;
          }
        }
        if (legalForCurrentTopMatchingItem) {
          Ranking currentInstanceRanking = new Ranking(model.getItemSet());
          for (Item e : list) {
            currentInstanceRanking.add(e);
          }
          
          p += model.getProbability(currentInstanceRanking);
          break;
        }
      }
    }
    return p;
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    Ranking center = items.getReferenceRanking();
    double phi = 0.6;
    MallowsModel model = new MallowsModel(center, phi);
    NaiveLabeledRIM posterior = new NaiveLabeledRIM(model);
    posterior.evaluateLabelQuery(null, null);
  }
}
