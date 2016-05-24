package edu.drexel.cs.db.rank.sampler.other;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.Set;

@Deprecated
public class AMPSamplerPlus extends MallowsSampler {

  private double alpha;
  private Sample<? extends PreferenceSet> sample;
  
  public AMPSamplerPlus(MallowsModel model, Sample sample, double alpha) {
    super(model);
    this.alpha = alpha;
    this.sample = sample;
  }
    
  
  private double[] support(PreferenceBuild build, int low, int high) {
    double[] support = new double[high+1];
    for (PW pw: sample) {
      int index = build.getInsertIndex(pw.p);
      if (index >= low && index <= high) {
        support[index] += pw.w;
      }
    }
    return support;
  }
  
  
  public Ranking sample(PreferenceSet v) {
    Ranking reference = model.getCenter();
    Ranking r = new Ranking(model.getItemSet());
    PreferenceSet tc = v.transitiveClosure();
    
    Item item = reference.get(0);
    r.add(item);
    PreferenceBuild build = new PreferenceBuild(tc, r, reference);
    
    for (int i = 1; i < reference.length(); i++) {
      item = reference.get(i);
      int low = 0;
      int high = i;
      
      Set<Item> higher = tc.getHigher(item);
      Set<Item> lower = tc.getLower(item);
      r = build.getPrefix();
      for (int j = 0; j < r.length(); j++) {
        Item it = r.get(j);
        if (higher.contains(it)) low = j + 1;
        if (lower.contains(it) && j < high) high = j;
      }
            
      if (low == high) {
        build = build.addNext(low);
      }
      else {              
        double[] support = support(build, low, high);
        double sc = MathUtils.sum(support);
        double a = sc / (alpha + sc);
        // Logger.info("Support: %f (%f)", sc, a);
        
        double[] p = new double[high+1];
        for (int j = low; j <= high; j++) {
          p[j] = Math.pow(model.getPhi(), i - j);
          if (a > 0) p[j] = (1 - a) * p[j] + a * support[j] / sc;
        }
        build = build.addNext(p);
      }
    }
    return build.getPrefix();
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking v = new Ranking(items);
    v.add(items.get(3));    
    v.add(items.get(7));
    v.add(items.get(5));
    System.out.println(v);
    
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.5);    
    RankingSample sample = MallowsUtils.sample(model, 10000);
    Filter.removeItems(sample, 0.2);
    
    AMPSamplerPlus sampler = new AMPSamplerPlus(model, sample, 100);
    Ranking r = sampler.sample(v);
    Logger.info("\nResult: %s", r);
  }


}
