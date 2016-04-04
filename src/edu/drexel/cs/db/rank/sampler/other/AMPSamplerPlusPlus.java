package edu.drexel.cs.db.rank.sampler.other;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.MallowsSampler;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Deprecated
public class AMPSamplerPlusPlus extends MallowsSampler {

  private double alpha;
  private final Sample<DensePreferenceSet> evidence;
  private final List<DensePreferenceSet> support = new ArrayList<DensePreferenceSet>();
  
  
  public AMPSamplerPlusPlus(MallowsModel model, Sample evidence, double alpha) {
    super(model);
    this.alpha = alpha;
    this.evidence = evidence.transitiveClosure();
  }
    
  
  private double[] support(PreferenceBuild build, int low, int high) {
    double[] sup = new double[high+1];
    support.clear();
    for (PW<DensePreferenceSet> pw: evidence) {
      int index = build.getInsertIndex(pw.p);
      if (index >= low && index <= high) {
        sup[index] += pw.w;
        support.add(pw.p);
      }
    }
    return sup;
  }
  
  private void updateEvidence(DensePreferenceSet tc, PreferenceBuild build) {
    Ranking r = build.getPrefix();
    Item item = build.getReference().get(r.length()-1);
    boolean seen = false;
    for (int i = 0; i < r.length(); i++) {
      Item it = r.get(i);
      if (it.equals(item)) seen = true;
      else if (seen) tc.add(item, it);
      else tc.add(it, item);
    }
  }
  
  private void updateSupport(PreferenceBuild build) {
    for (DensePreferenceSet dps: support) {
      updateEvidence(dps, build);
    }
  }

  @Override
  public Ranking sample(PreferenceSet pref) {
    throw new UnsupportedOperationException("Not supported for single sampling");
  }



  @Override
  public RankingSample sample(Sample sample) {
    Ranking reference = model.getCenter();
    Sample<DensePreferenceSet> tcs = sample.transitiveClosure();
    RankingSample out = new RankingSample(model.getItemSet());
    
    Ranking first = new Ranking(model.getItemSet());
    first.add(reference.get(0));
    List<PreferenceBuild> builds = new ArrayList<PreferenceBuild>();
    for (PW pw: tcs) {
      PreferenceBuild build = new PreferenceBuild(pw.p, first, reference);
      builds.add(build);
    }
    
    for (int i = 1; i < reference.length(); i++) {
      Item item = reference.get(i);
      
      for (int k = 0; k < builds.size(); k++) {
        PreferenceBuild build = builds.get(k);
        int low = 0;
        int high = i;

        PreferenceSet tc = build.getPreferenceSet();
        Set<Item> higher = tc.getHigher(item);
        Set<Item> lower = tc.getLower(item);
        Ranking r = build.getPrefix();
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

          double[] p = new double[high+1];
          for (int j = low; j <= high; j++) {
            p[j] = Math.pow(model.getPhi(), i - j);
            if (a > 0) p[j] = (1 - a) * p[j] + a * support[j] / sc;
          }
          build = build.addNext(p); 
          updateSupport(build);
        }
        
        if (build.getPrefix().length() == reference.length()) {
          out.add(build.getPrefix(), tcs.getWeight(k));
        }
        else {
          builds.set(k, build);
        }
      }
    }
    return out;
  }
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
//    Ranking v = new Ranking(items);
//    v.add(items.get(3));    
//    v.add(items.get(7));
//    v.add(items.get(5));
//    System.out.println(v);
    
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.5);    
    RankingSample sample = MallowsUtils.sample(model, 3);
    Filter.removeItems(sample, 0.2);
    System.out.println(sample);
    
    AMPSamplerPlusPlus sampler = new AMPSamplerPlusPlus(model, sample, 100);
    RankingSample rs = sampler.sample(sample);
    for (int i = 0; i < sample.size(); i++) {
      Logger.info("%s -> %s", sample.getPreferenceSet(i), rs.getPreferenceSet(i));
    }
  }


}
