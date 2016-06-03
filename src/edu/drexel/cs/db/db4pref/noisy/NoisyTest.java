package edu.drexel.cs.db.db4pref.noisy;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.sampler.MallowsUtils;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.util.Config;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;


public class NoisyTest {

  private final PrintWriter out;
  
  private final int[] itemSetSizes = { 10, 15, 20 };
  private final double[] phis = { 0.2, 0.5, 0.8 };
  private final double[] noises = { 0.1, 0.3, 0.5, 0.7 };
  private final int[] sampleSizes = { 1000, 2000, 5000, 10000 };
  
  private NoisyTest(File outFile) throws IOException {
    boolean header = !outFile.exists();
    out = FileUtils.append(outFile);
    if (header) {
      out.println("# NoisyTest");
      out.println("# items, sampleSize, resampleSize, phi, noise, phi_triangle, phi_boots_100, phi_boots_1k, phi_triangle_boots_100, phi_triangle_boots_1k, time_1, time_2, time_3, time_4, time_5");
      out.println("# " + new Date());
      out.flush();
    }
  }
  
  
  public void test() throws Exception {
    int test = 0;
    while (true) {
      test++;
      int itemSetSize = itemSetSizes[MathUtils.RANDOM.nextInt(itemSetSizes.length)];
      int sampleSize = sampleSizes[MathUtils.RANDOM.nextInt(sampleSizes.length)];
      double phi = phis[MathUtils.RANDOM.nextInt(phis.length)];
      double noise = noises[MathUtils.RANDOM.nextInt(noises.length)];


      Logger.info("Test #%d begin: %d, %d, %.1f, %.1f", test, itemSetSize, sampleSize, phi, noise);
      String line = String.format("%d\t%d\t%.1f\t%.1f", itemSetSize, sampleSize, phi, noise);


      // Prepare sample
      ItemSet items = new ItemSet(itemSetSize);
      Ranking center = items.getRandomRanking();
      MallowsModel model = new MallowsModel(center, phi);
      RankingSample sample = MallowsUtils.sample(model, sampleSize);
      Filter.noise(sample, noise);


      // First: Triangle no row
      long s = System.currentTimeMillis();
      NoisyReconstructor rec1 = new NoisyReconstructor(true, 0);
      MallowsModel m1 = rec1.reconstruct(sample);
      float t1 = 0.001f * (System.currentTimeMillis() - s);
      Logger.info("First: %.5f in %.1f sec", m1.getPhi(), t1);
      
      // Second: Bootstrap 100 only
      s = System.currentTimeMillis();
      NoisyReconstructor rec2 = new NoisyReconstructor(false, 100);
      MallowsModel m2 = rec2.reconstruct(sample);
      float t2 = 0.001f * (System.currentTimeMillis() - s);
      Logger.info("Second: %.5f in %.1f sec", m2.getPhi(), t2);
      
      // Third: Bootstrap 1k only
      s = System.currentTimeMillis();
      NoisyReconstructor rec3 = new NoisyReconstructor(false, 1000);
      MallowsModel m3 = rec3.reconstruct(sample);
      float t3 = 0.001f * (System.currentTimeMillis() - s);
      Logger.info("Third: %.5f in %.1f sec", m3.getPhi(), t3);
      
      // Fourth: Bootstrap triangle + 100
      s = System.currentTimeMillis();
      NoisyReconstructor rec4 = new NoisyReconstructor(true, 100);
      MallowsModel m4 = rec4.reconstruct(sample);
      float t4 = 0.001f * (System.currentTimeMillis() - s);
      Logger.info("Fourth: %.5f in %.1f sec", m4.getPhi(), t4);
      
      // Fifth: Bootstrap triangle + 1k
      s = System.currentTimeMillis();
      NoisyReconstructor rec5 = new NoisyReconstructor(true, 1000);
      MallowsModel m5 = rec5.reconstruct(sample);
      float t5 = 0.001f * (System.currentTimeMillis() - s);
      Logger.info("Fifth: %.5f in %.1f sec", m5.getPhi(), t5);
      
            
      line += String.format("\t%.5f\t%.5f\t%.5f\t%.5f\t%.5f", m1.getPhi(), m2.getPhi(), m3.getPhi(), m4.getPhi(), m5.getPhi());
      line += String.format("\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f", t1, t2, t3, t4, t5);

      Logger.info("Test #%d end  : " + line, test);
      out.println(line);
      out.flush();
    }
  }
  
  
  public static void main(String[] args) throws Exception {
    File out;
    if (args.length > 0) out = new File(args[0]);
    else out = new File(Config.RESULTS_FOLDER, "noisy.reconstruction.tsv");
    NoisyTest test = new NoisyTest(out);
    test.test();
  }
  
}
