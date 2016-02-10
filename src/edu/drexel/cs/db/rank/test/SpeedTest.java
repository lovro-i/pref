package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.incomplete.IncompleteGenerator;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.Expands;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.File;


public class SpeedTest {

  public static void main(String[] args) throws Exception {
    int n = 1000;
    ItemSet items = new ItemSet(n);
    
    Ranking center = items.getRandomRanking();
    double phi = 0.5;    
    MallowsModel model = new MallowsModel(center, phi);
    
    int sampleSize = 5000;
    Logger.info("Sampling %d...", sampleSize);
    long start1 = System.currentTimeMillis();
    Sample sample = MallowsUtils.sample(model, sampleSize);
    Logger.info("Sampled in %d sec", (System.currentTimeMillis() - start1) / 1000);
    // 200: 7 sec  | 257 sec
    // 300: 16 sec | 312 sec
    // 400: 28 sec | 465 sec | mem
    // 500: 43 sec | 870 sec
    // 1000: 175 sec | 
    
    double missing = 0.05;
    Logger.info("Removing %.2f", missing);
    Filter.remove(sample, missing);
    
    Expands.setThreshold(0.001);
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File arff = new File(folder, "incomplete.train.arff");
    IncompleteGenerator generator = new IncompleteGenerator(arff);
    
    Logger.info("%d items, %.2f missing", n, missing);
    
    long start = System.currentTimeMillis();
    generator.generateParallel(sample, 1);
    Logger.info("IncompleteGenerator done in %.1f sec", 1d * (System.currentTimeMillis() - start) / 1000);
    
  }
}
