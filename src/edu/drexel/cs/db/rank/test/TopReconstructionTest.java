package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.incomplete.BetterIncompleteTest;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.top.TopIncompleteReconstructor;
import edu.drexel.cs.db.rank.triangle.Expands;
import edu.drexel.cs.db.rank.util.Config;
import edu.drexel.cs.db.rank.util.FileUtils;
import java.io.File;
import java.io.PrintWriter;
import java.util.Date;


public class TopReconstructionTest {

  /** See accuracy by different combinations of parameters  */
  private static void testOne(double phi) throws Exception {
    File file = new File(Config.RESULTS_FOLDER, "top.reconstruction.tsv");
    
    boolean header = !file.exists();
    PrintWriter out = FileUtils.append(file);
    if (header) {
      out.println("# For a fixed number of items and phi, change top rate and see how phi reconstruction depends on features used");
      out.println("# items, sample_size, resample_size, train_series, triangle_threshold, top, minLength, maxLength, phi, phi_1, phi_2, phi_3, phi_13, phi_123, time_2, time_3, time_1, time_13, time_123");      
      out.println("# Created by edu.drexel.cs.db.rank.test.TopReconstructionTest.testOne()");
      out.println("# " + new Date());
      out.println();
      out.flush();
    }
    
    
    ItemSet items = new ItemSet(15);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    Expands.setThreshold(0.001);

    double[] tops = { 0.1, 0.3, 0.5, 0.7 };
    for (double top: tops) {
      Sample sample = MallowsUtils.sample(model, 5000);
      int min = (int) (top * items.size());
      int max = items.size();
      Filter.top(sample, min, max);
      int series = 3;
      int resampleSize = 10000;
      int bootstraps = 50;
      
      long s = System.currentTimeMillis();
      TopIncompleteReconstructor rec1 = new TopIncompleteReconstructor(true, false, false, 0, series);
      rec1.setResampleSize(resampleSize);
      MallowsModel m1 = rec1.reconstruct(sample);
      float t1 = 0.001f * (System.currentTimeMillis() - s);
      
      s = System.currentTimeMillis();
      TopIncompleteReconstructor rec2 = new TopIncompleteReconstructor(false, true, false, 0, series);
      rec2.setResampleSize(resampleSize);
      MallowsModel m2 = rec2.reconstruct(sample);
      float t2 = 0.001f * (System.currentTimeMillis() - s);
      
      s = System.currentTimeMillis();
      TopIncompleteReconstructor rec3 = new TopIncompleteReconstructor(false, false, true, 0, series);
      rec3.setResampleSize(resampleSize);
      MallowsModel m3 = rec3.reconstruct(sample);
      float t3 = 0.001f * (System.currentTimeMillis() - s);
      
      s = System.currentTimeMillis();
      TopIncompleteReconstructor rec13 = new TopIncompleteReconstructor(true, false, true, 0, series);
      rec13.setResampleSize(resampleSize);
      MallowsModel m13 = rec13.reconstruct(sample);
      float t13 = 0.001f * (System.currentTimeMillis() - s);
      
      s = System.currentTimeMillis();
      TopIncompleteReconstructor rec123 = new TopIncompleteReconstructor(true, true, true, 0, series);
      rec123.setResampleSize(resampleSize);
      MallowsModel m123 = rec123.reconstruct(sample);
      float t123 = 0.001f * (System.currentTimeMillis() - s);

      // items, sample_size, resample_size, train_series, triangle_threshold, top, minLength, maxLength, phi, phi_1, phi_2, phi_3, phi_13, phi_123, time_2, time_3, time_1, time_13, time_123
      String line = String.format("%d\t%d\t%d\t%d\t%f", items.size(), sample.size(), resampleSize, series, Expands.getThreshold());
      line += String.format("\t%.2f\t%d\t%d", top, min, max);
      line += String.format("\t%.2f\t%f\t%f\t%f\t%f\t%f", model.getPhi(), m1.getPhi(), m2.getPhi(), m3.getPhi(), m13.getPhi(), m123.getPhi());
      line += String.format("\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f", t1, t2, t3, t13, t123);
      out.println(line);
      out.flush();
    }
    
    out.close();
  }
  
  
  public static void main(String[] args) throws Exception {
    for (int i = 1; i < 100; i++) {      
      testOne(0.2);
      testOne(0.5);      
      testOne(0.8);
      if (i % 4 == 0) {
        BetterIncompleteTest.testOne(0.2);
        BetterIncompleteTest.testOne(0.8);
      }
    }  
  }
  
  
  
}
