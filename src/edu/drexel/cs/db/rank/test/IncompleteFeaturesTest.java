package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.triangle.Expands;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;


/** Test which features are important for incomplete reconstructor */
public class IncompleteFeaturesTest {

  public static void main(String[] args) {
    ElementSet elements = new ElementSet(10);
    Sample sample = MallowsUtils.sample(elements.getRandomRanking(), 0.2, 10000);
    Filter.remove(sample, 0.3);
    int resampleSize = 5000;
    
    
    Ranking center = CenterReconstructor.reconstruct(sample);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    
    // triangle no row
    {
      Expands.setThreshold(0.001d);
      SampleTriangle st = new SampleTriangle(center, sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      System.out.println(mallows.getPhi());
    }
    
  }
  
}
