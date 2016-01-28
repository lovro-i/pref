package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.datasets.Sushi;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.filter.NonDesctructiveFilter;
import edu.drexel.cs.db.rank.filter.Split;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureReconstructor;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.Expands;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.TrainUtils;
import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;


public class BetterIncompleteTest {

  /** See accuracy by different combinations of parameters  */
  private static void testOne() throws Exception {
    // File folder = new File("C:\\Projects\\Rank\\Results.3");
    File folder = new File("/home/lovro/rank/results");
    File file = new File(folder, "incomplete.reconstruction.tsv");
    
    boolean header = !file.exists();
    PrintWriter out = FileUtils.append(file);
    if (header) {
      out.println("# For a fixed number of items and phi, change missing rate and see how phi reconstruction depends on features used");
      out.println("# items, sample_size, resample_size, train_series, triangle_threshold, bootstraps, missing_rate, phi, phi_1, phi_2, phi_3, phi_13, phi_123, time_2, time_3, time_1, time_13, time_123");      
      out.println("# Created by edu.drexel.cs.db.rank.incomplete.BetterIncompleteTest.testOne()");
      out.println("# " + new Date());
      out.println();
      out.flush();
    }
    
    
    ItemSet items = new ItemSet(15);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.5);
    Expands.setThreshold(0.001);

    double[] misses = TrainUtils.step(0.1, 0.9, 0.1);
    for (double miss: misses) {
      Sample sample = MallowsUtils.sample(model, 5000);
      Filter.remove(sample, miss);
      int series = 3;
      int resampleSize = 10000;
      int bootstraps = 50;
      
      long s = System.currentTimeMillis();
      BetterIncompleteReconstructor rec1 = new BetterIncompleteReconstructor(true, false, 0, series);
      rec1.setResampleSize(resampleSize);
      MallowsModel m1 = rec1.reconstruct(sample);
      float t1 = 0.001f * (System.currentTimeMillis() - s);
      
      s = System.currentTimeMillis();
      BetterIncompleteReconstructor rec2 = new BetterIncompleteReconstructor(false, true, 0, series);
      rec2.setResampleSize(resampleSize);
      MallowsModel m2 = rec2.reconstruct(sample);
      float t2 = 0.001f * (System.currentTimeMillis() - s);
      
      s = System.currentTimeMillis();
      BetterIncompleteReconstructor rec3 = new BetterIncompleteReconstructor(false, false, bootstraps, series);
      rec3.setResampleSize(resampleSize);
      MallowsModel m3 = rec3.reconstruct(sample);
      float t3 = 0.001f * (System.currentTimeMillis() - s);
      
      s = System.currentTimeMillis();
      BetterIncompleteReconstructor rec13 = new BetterIncompleteReconstructor(true, false, bootstraps, series);
      rec13.setResampleSize(resampleSize);
      MallowsModel m13 = rec13.reconstruct(sample);
      float t13 = 0.001f * (System.currentTimeMillis() - s);
      
      s = System.currentTimeMillis();
      BetterIncompleteReconstructor rec123 = new BetterIncompleteReconstructor(true, true, bootstraps, series);
      rec123.setResampleSize(resampleSize);
      MallowsModel m123 = rec123.reconstruct(sample);
      float t123 = 0.001f * (System.currentTimeMillis() - s);

      String line = String.format("%d\t%d\t%d\t%d\t%f\t%d\t%.2f", items.size(), sample.size(), resampleSize, series, Expands.getThreshold(), bootstraps, miss);
      line += String.format("\t%.2f\t%f\t%f\t%f\t%f\t%f", model.getPhi(), m1.getPhi(), m2.getPhi(), m3.getPhi(), m13.getPhi(), m123.getPhi());
      line += String.format("\t%.1f\t%.1f\t%.1f\t%.1f\t%.1f", t1, t2, t3, t13, t123);
      out.println(line);
      out.flush();
    }
    
    out.close();
  }
  
  
  /** See accuracy by different combinations of parameter on alpha */
  private static void testSushi() throws Exception {
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File file = new File(folder, "incomplete.sushi.tsv");
    
    boolean header = !file.exists();
    PrintWriter out = FileUtils.append(file);
    if (header) {
      out.println("# See how sushi model reconstruction depends on percentage of missing elements");
      out.println("# train_size, test_size, resample_size, train_series, triangle_threshold, bootstraps, missing_rate, clusters, ll_weight_test, ll_max_test, kl_train, kl_test");      
      out.println("# Created by edu.drexel.cs.db.rank.incomplete.BetterIncompleteTest.testSushi()");
      out.println("# " + new Date());
      out.println();
      out.flush();
    }
    
    Sushi sushi = new Sushi("C:\\Projects\\Rank\\Data\\sushi\\sushi3a.csv");
    Expands.setThreshold(0.001);

    double[] misses = TrainUtils.step(0, 0.9, 0.1);
    for (double miss: misses) {
      List<Sample> samples = Split.twoFold(sushi.getSample(), 0.7);
      Sample trainSample = NonDesctructiveFilter.remove(samples.get(0), miss);
      Sample testSample = samples.get(1);
      
      int series = 3;
      int resampleSize = 10000;
      int bootstraps = 50;
      
      long s = System.currentTimeMillis();
      BetterIncompleteReconstructor rec1 = new BetterIncompleteReconstructor(true, true, bootstraps, series);
      rec1.setResampleSize(resampleSize);
      MallowsMixtureReconstructor mmr1 = new MallowsMixtureReconstructor(rec1, 10);
      MallowsMixtureModel model = mmr1.reconstruct(trainSample);
      

      // double llwTrain = model.getLogLikelihoodMean(trainSample);
      double llwTest = model.getLogLikelihoodMean(testSample);
      // double llmTrain = model.getLogLikelihoodMax(trainSample);
      double llmTest = model.getLogLikelihoodMax(testSample);
      
      Sample modelSample = MallowsUtils.sample(model, 100000);
      double klTrain = edu.drexel.cs.db.rank.measure.KullbackLeibler.divergence(trainSample, modelSample);
      double klTest = edu.drexel.cs.db.rank.measure.KullbackLeibler.divergence(testSample, modelSample);

      String line = String.format("%d\t%d\t%d\t%d\t%f\t%d\t%.2f\t%d", trainSample.size(), testSample.size(), resampleSize, series, Expands.getThreshold(), bootstraps, miss, model.size());
      line += String.format("\t%f\t%f\t%f\t%f", llwTest, llmTest, klTrain, klTest);
      out.println(line);
      out.flush();
    }
    
    out.close();
  }
  
  
  
  public static void main(String[] args) throws Exception {
    for (int i = 0; i < 10; i++) {
      testOne();
    }    
  }
}
