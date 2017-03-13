/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.posterior.labeled;

import com.google.common.collect.Sets;
import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.sequential2.Expander2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

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

  public LabeledRIM(MallowsModel model) {
    this.model = model;
  }

  public double evaluateLabelQuery(ItemsetPreferences itemsetPrefs, LabelRanges labelRanges) {
    
    // The partial order of labels is first converted into a set of rankings.
    // A ranking of labels is denoted by a list of their itemset ID.
    Set<List<Integer>> linearExtensions = linearExtensions(itemsetPrefs.getItemSetPreferences());
    
    // start running RIM sampling
    double p = 0;
    // for each topMatching item combination
    for (List<Item> topMatchingItems : Sets.cartesianProduct(itemsetPrefs.getItemSetList())) {
      // for each sub-ranking compatible with the partial order
      for (List<Integer> linearExtension: linearExtensions){
        
        Map<Integer, Integer> latestParents = latestParents(linearExtension, itemsetPrefs.getItemSetPreferences());
        
        // subRanking is an item-level ranking for label-level rankings.
        Ranking initialRanking = new Ranking(model.getItemSet());
        for(Integer labelNumber: linearExtension){
          initialRanking.add(topMatchingItems.get(labelNumber));
        }
        
        Expander expander = new Expander(model, initialRanking, itemsetPrefs, topMatchingItems, latestParents, labelRanges);
        p += expander.expand();
      }
    }

    return p;
  }
  
  public Map<Integer, Integer> latestParents(List<Integer> labelRanking, Set<List<Integer>> labelPrefs){
    Map<Integer, Integer> latestParents = new HashMap<>();
    for(List<Integer> pair: labelPrefs){
      int parent = pair.get(0);
      int child = pair.get(1);
      int parentPosition = labelRanking.indexOf(parent);
      if(latestParents.containsKey(child)){
        int tempParentPosition = labelRanking.indexOf(latestParents.get(child));
        if (parentPosition>tempParentPosition) {
          latestParents.put(child, parent);
        }
      } else {
        latestParents.put(child, parent);
      }
    }
    return latestParents;
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

  public static void main(String[] args) throws TimeoutException, InterruptedException {
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

    ItemsetPreferences itemSetPrefs = new ItemsetPreferences();
    itemSetPrefs.add(set1, set2);
    itemSetPrefs.add(set1, set3);

    Ranking center = items.getReferenceRanking();
    double phi = 0.6;
    MallowsModel model = new MallowsModel(center, phi);
    LabeledRIM posterior = new LabeledRIM(model);
    System.out.println(posterior.evaluateLabelQuery(itemSetPrefs,null));
    
    MapPreferenceSet pref = new MapPreferenceSet(items);
    pref.addById(8, 2);
    pref.addById(8, 1);
    Expander2 expander2 = new Expander2(model, pref);
    System.out.println(expander2.expand());
  }
}
