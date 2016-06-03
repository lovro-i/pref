package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.sampler.MallowsUtils;
import edu.drexel.cs.db.db4pref.sampler.RIMRSampler;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.db4pref.triangle.Expands;
import edu.drexel.cs.db.db4pref.triangle.SampleTriangle;


/** Test which features are important for incomplete reconstructor */
public class IncompleteFeaturesTest {

  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    RankingSample sample = MallowsUtils.sample(items.getRandomRanking(), 0.2, 10000);
    Filter.removeItems(sample, 0.3);
    int resampleSize = 5000;
    
    
    Ranking center = CenterReconstructor.reconstruct(sample);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    
    // triangle no row
    {
      Expands.setThreshold(0.001d);
      SampleTriangle st = new SampleTriangle(center, sample);
      RIMRSampler resampler = new RIMRSampler(st);
      RankingSample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      System.out.println(mallows.getPhi());
    }
    
  }
  
}
