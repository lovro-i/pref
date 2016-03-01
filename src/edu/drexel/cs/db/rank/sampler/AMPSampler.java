package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.Map;
import java.util.Set;


public class AMPSampler extends MallowsSampler {

  
  public AMPSampler(MallowsModel model) {
    super(model);
  }
  
  
   
  @Override
  public Ranking sample(PreferenceSet v) {
    Ranking reference = model.getCenter();
    Ranking r = new Ranking(model.getItemSet());
    DensePreferenceSet tc = v.transitiveClosure();
    
    Item item = reference.get(0);
    r.add(item);
    for (int i = 1; i < reference.size(); i++) {
      item = reference.get(i);
      int low = 0;
      int high = i;
      
      long t1 = System.currentTimeMillis();
      Set<Item> higher = tc.getHigher(item);
      Set<Item> lower = tc.getLower(item);
      for (int j = 0; j < r.size(); j++) {
        Item it = r.get(j);
        if (higher.contains(it)) low = j + 1;
        if (lower.contains(it) && j < high) high = j;
      }
            
      if (low == high) {
        r.add(low, item);
      }
      else {
        long t2 = System.currentTimeMillis();
        double sum = 0;
        double[] p = new double[high+1];                
        for (int j = low; j <= high; j++) {
          p[j] = Math.pow(model.getPhi(), i - j);
          sum += p[j];
        }
        
        double flip = MathUtils.RANDOM.nextDouble();
        double ps = 0;
        for (int j = low; j <= high; j++) {
          ps += p[j] / sum;
          if (ps > flip || j == high) {
            r.add(j, item);
            break;
          }
        }
      }
    }
    return r;
  }
  
  

  public RankingSample sample(PreferenceSet v, int size) {
    RankingSample sample = new RankingSample(model.getItemSet());
    for (int i = 0; i < size; i++) {
      sample.add(sample(v));      
    }
    return sample;
  }
  
  public Ranking sample(Ranking v) {
    Ranking reference = model.getCenter();
    Map<Item, Integer> map = v.getIndexMap();
    Ranking r = new Ranking(model.getItemSet());
    
    Item item = reference.get(0);
    r.add(item);
    for (int i = 1; i < reference.size(); i++) {
      item = reference.get(i);
      int low, high;
      
      Integer ci = map.get(item);
      if (ci == null) {
        low = 0;
        high = i;
      }
      else {
        low = 0;
        high = i;
        
        for (int j = 0; j < r.size(); j++) {
          Item t = r.get(j);
          Integer ti = map.get(t);
          if (ti == null) continue;
          if (ti < ci) low = j + 1;
          if (ti > ci && j < high) high = j;
        }
      }
      
      if (low == high) {
        r.add(low, item);
      }
      else {        
        double sum = 0;
        double[] p = new double[high+1];                
        for (int j = low; j <= high; j++) {
          p[j] = Math.pow(model.getPhi(), i - j);
          sum += p[j];
        }
        
        double flip = MathUtils.RANDOM.nextDouble();
        double ps = 0;
        for (int j = low; j <= high; j++) {
          ps += p[j] / sum;
          if (ps > flip || j == high) {
            r.add(j, item);
            break;
          }
        }
      }
    }
    return r;
  }
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking v = new Ranking(items);
    v.add(items.get(3));    
    v.add(items.get(7));
    v.add(items.get(5));
    System.out.println(v);
    
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.8);
    AMPSampler sampler = new AMPSampler(model);
    RankingSample sample = sampler.sample(v, 1000);
//    System.out.println(sample);
//    for (Ranking r: sample) {
//      if (!Filter.isConsistent(r, v)) Logger.error("Inconsistent: " + r.toString());
//    }
    
    
    // PreferenceSet test
    {
      RankingSample s2 = sampler.sample(v.transitiveClosure(), 1000);
      System.out.println(s2);
      for (Ranking r: s2.rankings()) {
        if (!r.isConsistent(v)) Logger.error("Inconsistent: " + r.toString());
      }
    }
    
  }
}
