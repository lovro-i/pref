package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.distance.KL;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;


public class SamplerTester {

  
  public static double test(MallowsSampler testSampler, MallowsModel model, PreferenceSet pref, int count) {
    
    MallowsSampler realSampler = new RejectionSampler(model);
    RankingSample realSample = realSampler.sample(pref, count);
    
    long start = System.currentTimeMillis();
    testSampler.setModel(model);
    RankingSample testSample = testSampler.sample(pref, count);
    
    double kl = KL.divergence(realSample, testSample);
    Logger.info("%s KL: %f in %.1f sec", testSampler.getClass().getSimpleName(), kl, 0.001d * (System.currentTimeMillis() - start));
    return kl;
  }
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking reference = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(reference, 0.3);
    
    Ranking v = new Ranking(items);
    v.add(items.get(3));    
    v.add(items.get(7));
    v.add(items.get(5));
    System.out.println(v);
    
    {      
      RejectionSampler testSampler = new RejectionSampler(model);
      test(testSampler, model, v, 100000);
    }
    
    {
      AMPSampler testSampler = new AMPSampler(model);
      test(testSampler, model, v, 100000);
    }
    
    {
      RankingSample trainSample = MallowsUtils.sample(model, 10000);
      Filter.remove(trainSample, 0.3);
      AMPSamplerX testSampler = new AMPSamplerX(model, trainSample, 100);
      test(testSampler, model, v, 100000);
    }
    
    {
      RankingSample trainSample = MallowsUtils.sample(model, 10000);
      Filter.remove(trainSample, 0.3);
      AMPSamplerPlus testSampler = new AMPSamplerPlus(model, trainSample, 100);
      test(testSampler, model, v, 100000);
    }
    
    {
      RankingSample trainSample = MallowsUtils.sample(model, 10000);
      Filter.remove(trainSample, 0.3);
      AMPSamplerPlusPlus testSampler = new AMPSamplerPlusPlus(model, trainSample, 100);
      test(testSampler, model, v, 100000);
    }
    
  }
}
