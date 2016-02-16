package edu.drexel.cs.db.rank.sampler;

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
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class AMPSamplerPlusPlus implements MallowsSampler {

  private MallowsModel model;
  private double alpha;
  
  public AMPSamplerPlusPlus(MallowsModel model, double alpha) {
    this.model = model;
    this.alpha = alpha;
  }
    
  
  @Override
  public MallowsModel getModel() {
    return model;
  }
  
  public void setModel(MallowsModel model) {
    this.model = model;
  }
  
  
  private double[] support(Sample<DensePreferenceSet> evidence, PreferenceBuild build, int low, int high) {
    double[] support = new double[high+1];
    for (PW pw: evidence) {
      int index = build.getInsertIndex(pw.p);
      if (index >= low && index <= high) {
        support[index] += pw.w;
        // Logger.info("Found support: %s", pw.p);
      }
    }
    return support;
  }
  
  private void updateEvidence(DensePreferenceSet tc, PreferenceBuild build) {
    // System.out.println(build);
    // Logger.info("\nBefore update: %s", tc);
    Ranking r = build.getPrefix();
    Item item = build.getReference().get(r.size()-1);
    boolean seen = false;
    for (int i = 0; i < r.size(); i++) {
      Item it = r.get(i);
      if (it.equals(item)) seen = true;
      else if (seen) tc.add(item, it);
      else tc.add(it, item);
    }
    // Logger.info("After update: %s", tc);
  }
  

  @Override
  public Ranking sample(PreferenceSet pref) {
    throw new UnsupportedOperationException("Not supported yet.");
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
    
    for (int i = 1; i < reference.size(); i++) {
      Item item = reference.get(i);
      
      for (int k = 0; k < builds.size(); k++) {
        PreferenceBuild build = builds.get(k);
        int low = 0;
        int high = i;

        PreferenceSet tc = build.getPreferenceSet();
        Set<Item> higher = tc.getHigher(item);
        Set<Item> lower = tc.getLower(item);
        Ranking r = build.getPrefix();
        for (int j = 0; j < r.size(); j++) {
          Item it = r.get(j);
          if (higher.contains(it)) low = j + 1;
          if (lower.contains(it) && j < high) high = j;
        }

        if (low == high) {
          build = build.addNext(low);
        }
        else {              
          double[] support = support(tcs, build, low, high);
          double sc = MathUtils.sum(support);
          double a = sc / (alpha + sc);

          double[] p = new double[high+1];
          for (int j = low; j <= high; j++) {
            p[j] = Math.pow(model.getPhi(), i - j);
            if (a > 0) p[j] = (1 - a) * p[j] + a * support[j] / sc;
          }
          build = build.addNext(p);          
        }
        
        if (build.getPrefix().size() == reference.size()) {
          out.add(build.getPrefix(), tcs.getWeight(k));
        }
        else {
          updateEvidence(tcs.getPreferenceSet(k), build);
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
    RankingSample sample = MallowsUtils.sample(model, 1);
    Filter.remove(sample, 0.2);
    System.out.println(sample);
    
    AMPSamplerPlusPlus sampler = new AMPSamplerPlusPlus(model, 100);
    RankingSample rs = sampler.sample(sample);
    for (int i = 0; i < sample.size(); i++) {
      Logger.info("%s -> %s", sample.getPreferenceSet(i), rs.getPreferenceSet(i));
    }
  }


}
