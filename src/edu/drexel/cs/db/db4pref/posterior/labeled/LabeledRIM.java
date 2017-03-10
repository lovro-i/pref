/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.posterior.labeled;

import com.google.common.collect.Sets;
import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Labeled RIM is a RIM model for items with labels. Labels describe some attributes of the items. For example,
 * a person is 30 years old. Here 30 is a label for the age attribute of the person. Currently it only supports
 * Mallows Model.
 * 
 * Labeled RIM supports label preferences (as path) and min-max of labels. Example query,
 *  Q():P(v;c1,c2),V(v, sex),C(c1,sex,_),C(c2,_,age), age &gt; 30, c1.max &gt; 20, c2.min &lt; 5
 * where v is voter, c1 is candidate1, c2 is candidate2. The query is to compute the probability of a voter prefers
 * to a candidate of the same sex to a candidate older than 30.
 *
 * @author hping
 */
public class LabeledRIM {

  protected final MallowsModel model;
  private ItemSetPreferences itemSetPrefs;
  private Map<Integer, Item> topMatching = new HashMap<>(); // ItemSet number -> Item

  public LabeledRIM(MallowsModel model) {
    this.model = model;
  }

  public double evaluateLabelQuery(ItemSetPreferences itemSetPrefs, LabelRanges labelRanges) {
    this.itemSetPrefs = itemSetPrefs;

    // list of itemset numbers
    Set<List<Integer>> linearExtensions = linearExtensions(itemSetPrefs.getItemSetPreferences());
    
    double p = 0;
    // for each combination of indicator items
    for (List<Item> topMatchingList : Sets.cartesianProduct(itemSetPrefs.getItemSetList())) {
      // for each compatible sub-ranking of the partial order
      for (List<Integer> linearExtension: linearExtensions){
        
        // compute the sub-ranking
        Ranking subRanking = new Ranking(model.getItemSet());
        for (Integer i: linearExtension){
          subRanking.add(topMatchingList.get(i));
        }
        System.out.println(subRanking);
        
        Expander expander = new Expander();
        
      }
    }

    return p;
  }

  /**
   * Compute compatible rankings of a partial order of labels. Such rankings are as input in TopProb algorithm.
   * @param prefs a set of pairwise preferences
   * @return 
   */
  public Set<List<Integer>> linearExtensions(Set<List<Integer>> prefs) {
    Set<List<Integer>> s = new HashSet<>();
    List<Integer> tmp = new ArrayList<>();
    tmp.add(prefs.iterator().next().get(0));
    s.add(tmp);
    for (List<Integer> pref : prefs) {
      int high = pref.get(0);
      int low = pref.get(1);
      for (List<Integer> ranking : s) {
        int highIdx = ranking.indexOf(high);
        int lowIdx = ranking.indexOf(low);
        if (highIdx == -1 && lowIdx == -1) {
          for (int i = 0; i < ranking.size(); i++) {
            for (int j = i + 1; j <= ranking.size() + 1; j++) {
              List<Integer> r = new ArrayList<>(ranking);
              r.add(i, high);
              r.add(j, low);
              s.add(r);
            }
          }
        } else if (highIdx == -1) {
          for (int i = 0; i <= lowIdx; i++) {
            List<Integer> r = new ArrayList<>(ranking);
            r.add(i, high);
            s.add(r);
          }
        } else if (lowIdx == -1) {
          for (int j = highIdx + 1; j <= ranking.size(); j++) {
            List<Integer> r = new ArrayList<>(ranking);
            r.add(j, low);
            s.add(r);
          }
        }
        s.remove(ranking);
      }
    }
    return s;
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Set<Item> set1 = new HashSet<>();
    set1.add(items.get(0));
    set1.add(items.get(8));
    Set<Item> set2 = new HashSet<>();
    set2.add(items.get(2));
    set2.add(items.get(5));
    Set<Item> set3 = new HashSet<>();
    set3.add(items.get(1));
    set3.add(items.get(7));
    set3.add(items.get(6));

    ItemSetPreferences itemSetPrefs = new ItemSetPreferences();
    itemSetPrefs.add(set1, set2);
    itemSetPrefs.add(set1, set3);

    Ranking center = items.getReferenceRanking();
    double phi = 0.6;
    MallowsModel model = new MallowsModel(center, phi);
    LabeledRIM posterior = new LabeledRIM(model);
    posterior.evaluateLabelQuery(itemSetPrefs,null);
  }
}
