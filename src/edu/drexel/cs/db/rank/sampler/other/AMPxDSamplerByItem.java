package edu.drexel.cs.db.rank.sampler.other;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.AMPSampler;
import edu.drexel.cs.db.rank.sampler.AMPxSampler;
import edu.drexel.cs.db.rank.triangle.TriangleRow;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.HashSet;
import java.util.Set;

/** The one that uses the whole sample for probabilities and updates the IPM after each new sample. */
@Deprecated
public class AMPxDSamplerByItem extends AMPxSampler {

  /** Very low rate (close to zero) favors sample information.
   * High rate (close to positive infinity) favors AMP.
   * 
   * @param model
   * @param sample
   * @param rate 
   */
  public AMPxDSamplerByItem(MallowsModel model, Sample sample, double rate) {
    super(model, sample, rate);
  }

  public RankingSample sample(PreferenceSet pref, int count) {
    RankingSample out = new RankingSample(pref.getItemSet());
    for (int i = 0; i < count; i++) {
      Ranking r = sample(pref);
      out.add(r);
      this.addTrainingSample(r, 1);
    }
    return out;
  }
  
  private Ranking sampleOne(PreferenceSet tc, Ranking r, int index) {
    Ranking reference = model.getCenter();
    Item item = reference.get(index);
    // r.add(item);

    int low = 0;
    int high = index;

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
      double beta = 0;
      TriangleRow row = null;
      if (triangle != null) {
        row = triangle.getRow(index);
        beta = row.getSum() / (alpha + row.getSum()); // how much should the sample be favored
      }
      for (int j = low; j <= high; j++) {
        p[j] = Math.pow(model.getPhi(), index - j);
        if (row != null && beta > 0) p[j] = (1 - beta) * p[j] + beta * row.getProbability(j);
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
    return r;
  }
  
  /** Create new sample with completions of the rankings in the input one */
  public RankingSample sample(Sample<? extends PreferenceSet> sample) {
    Ranking reference = model.getCenter();
    Sample<PreferenceSet> in = sample.transitiveClosure();
    
    
    Item first = reference.get(0);
    Set<Item> items = new HashSet<Item>();
    items.add(first);
    
    // Init out sample
    RankingSample out = new RankingSample(sample.getItemSet());
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = new Ranking(sample.getItemSet());
      r.add(first);
      out.add(r, sample.getWeight(i));      
    }
    
    
    for (int index = 1; index < reference.length(); index++) {
      Item item = reference.get(index);
      items.add(item);
      
      for (int i = 0; i < in.size(); i++) {
        PreferenceSet tc = in.get(i).p;
        Ranking r = out.get(i).p;
        double w = sample.getWeight(i);
        sampleOne(tc, r, index);
        int add = r.indexOf(item);
        triangle.add(index, add, w);
      }
    }
    
    return out;
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
    
    AMPxDSamplerByItem sampler = new AMPxDSamplerByItem(model, s1, 10);
    RankingSample sample = sampler.sample(v, 1000);
    System.out.println(sample);
  }
}
