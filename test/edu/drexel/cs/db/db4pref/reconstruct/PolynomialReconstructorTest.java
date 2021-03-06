package edu.drexel.cs.db.db4pref.reconstruct;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.sampler.MallowsUtils;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals;

public class PolynomialReconstructorTest {
  

  /**
   * Test of reconstruct method, of class PolynomialReconstructor.
   */
  @Test
  public void testReconstruct_Sample() {
    int n = 20;
    ItemSet items = new ItemSet(n);
    
    for (int i = 0; i < 20; i++) {
      double phi = Math.random() * 0.8;
      Ranking center = items.getRandomRanking();
      MallowsModel model = new MallowsModel(center, phi);

      int sampleSize = 5000;
      RankingSample sample = MallowsUtils.sample(model, sampleSize);

      PolynomialReconstructor rec = new PolynomialReconstructor();
      MallowsModel mm = rec.reconstruct(sample, center);
      System.out.println(model);
      System.out.println(mm);
      assertEquals(center, mm.getCenter());
      double d = Math.abs(phi - mm.getPhi());
      assert(d < 0.01);
    }
  }


  
}
