package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.triangle.ConfidentTriangle;
import edu.drexel.cs.db.rank.triangle.TriangleRow;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.Map;
import java.util.Set;

/** The one that uses the whole sample for probabilities. */
public class AMPSamplerX extends MallowsSampler {

  protected ConfidentTriangle triangle;
  protected double rate;
  
  /** Very low rate (close to zero) favors sample information.
   * High rate (close to positive infinity) favors AMP.
   * 
   * @param model
   * @param sample
   * @param rate 
   */
  public AMPSamplerX(MallowsModel model, Sample sample, double rate) {
    super(model);
    this.setTrainingSample(sample);
    if (rate < 0) throw new IllegalArgumentException("Rate must be greater or equal zero");
    this.rate = rate;
  }


  
  public final void setTrainingSample(Sample sample) {
    this.triangle = new ConfidentTriangle(model.getCenter(), sample);
  }
  
  public void addTrainingSample(Sample sample) {
    if (triangle == null) setTrainingSample(sample);
    else triangle.add(sample);
  }
  
  /** Add single PreferenceSet to training sample (triangle) */
  public void addTrainingSample(PreferenceSet pref, double weight) {
    if (triangle == null) {
      Sample sample = new Sample(pref.getItemSet());
      sample.add(pref, weight);
      setTrainingSample(sample);
    }
    else {
      triangle.add(pref, weight);
    }
  }
  
  public void setRate(double rate) {
    this.rate = rate;
  }
  
  
  public Ranking sample(PreferenceSet v) {
    Ranking reference = model.getCenter();
    Ranking r = new Ranking(model.getItemSet());
    PreferenceSet tc = v.transitiveClosure();
    
    Item item = reference.get(0);
    r.add(item);
    for (int i = 1; i < reference.length(); i++) {
      item = reference.get(i);
      int low = 0;
      int high = i;
      
      Set<Item> higher = tc.getHigher(item);
      Set<Item> lower = tc.getLower(item);
      for (int j = 0; j < r.length(); j++) {
        Item it = r.get(j);
        if (higher.contains(it)) low = j + 1;
        if (lower.contains(it) && j < high) high = j;
      }
            
      if (low == high) {
        r.add(low, item);
      }
      else {        
        double sum = 0;
        double[] p = new double[high+1];
        double alpha = 0;
        TriangleRow row = null;
        if (triangle != null) {
          row = triangle.getRow(i);
          alpha = row.getSum() / (rate + row.getSum()); // how much should the sample be favored
        }
        for (int j = low; j <= high; j++) {
          p[j] = Math.pow(model.getPhi(), i - j);
          if (row != null && alpha > 0) p[j] = (1 - alpha) * p[j] + alpha * row.getProbability(j);
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
  

  public Ranking sample(Ranking v) {
    Ranking reference = model.getCenter();
    Map<Item, Integer> map = v.getIndexMap();
    Ranking r = new Ranking(model.getItemSet());
    
    Item item = reference.get(0);
    r.add(item);
    for (int i = 1; i < reference.length(); i++) {
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
        
        for (int j = 0; j < r.length(); j++) {
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
        TriangleRow row = null;
        double alpha = 0;
        if (triangle != null) {
          row = triangle.getRow(i);
          alpha = row.getSum() / (rate + row.getSum()); // how much should the sample be favored
        }
        for (int j = low; j <= high; j++) {
          p[j] = Math.pow(model.getPhi(), i - j);
          if (row != null && alpha > 0) p[j] = (1 - alpha) * p[j] + alpha * row.getProbability(j);
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
    v.add(items.get(0));    
    v.add(items.get(1));    
    v.add(items.get(3));    
    v.add(items.get(7));
    v.add(items.get(5));
    System.out.println(v);
    
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.8);
    AMPSampler amp = new AMPSampler(model);
    RankingSample s1 = amp.sample(v, 1000);
    
    
    
    AMPSamplerX sampler = new AMPSamplerX(model, s1, 10);
    RankingSample sample = sampler.sample(v, 1000);
    
  }

  
}
