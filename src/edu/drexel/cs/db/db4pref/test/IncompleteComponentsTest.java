package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.filter.SampleCompleter;
import edu.drexel.cs.db.db4pref.sampler.MallowsUtils;
import edu.drexel.cs.db.db4pref.sampler.RIMRSampler;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.db4pref.triangle.SampleTriangle;
import edu.drexel.cs.db.db4pref.triangle.SampleTriangleByRow;
import edu.drexel.cs.db.db4pref.util.Config;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class IncompleteComponentsTest {

  private final PrintWriter out;
  private final int threads;
  
  private final int[] itemSetSizes = { 10, 15, 20 };
  private final double[] phis = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 };
  private final double[] misses = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8 };
  private final int[] sampleSizes = { 1000, 2000, 5000, 10000 };
  private final int resampleSize = 50000;
  private final PolynomialReconstructor reconstructor = new PolynomialReconstructor();
  
  private IncompleteComponentsTest(File outFile, int threads) throws IOException {
    this.threads = threads;
    
    boolean header = !outFile.exists();
    out = FileUtils.append(outFile);
    if (header) {
      out.println("# IncompleteComponentsTest");
      out.println("# items, sampleSize, resampleSize, phi, miss, phi_1, time_1 (sec), phi_2, time_2 (sec), phi_bootstrap_200, var_bootstrap_200, time_bootstrap_200 (sec)");
      out.println("# " + new Date());
      out.flush();
    }
  }
  
  
  private synchronized void write(String line) {
    out.println(line);
    out.flush();
  }
  
  public void test() throws IOException, InterruptedException {    
    List<Worker> workers = new ArrayList<Worker>();
    for (int i = 0; i < threads; i++) {
      Worker worker = new Worker();
      worker.start();
      workers.add(worker);
    }
    
    for (Worker worker: workers) worker.join();
    out.close();
  }
   
  private int nextId = 1;
  private int test = 0;
  
  private int nextTest() {
    return ++test;
  }
  
  private class Worker extends Thread {
    
    private int id;
        
    private Worker() {
      this.id = nextId++;
    }
    
    public void run() {
      while (true) {
        int test = nextTest();
        int itemSetSize = itemSetSizes[MathUtils.RANDOM.nextInt(itemSetSizes.length)];
        int sampleSize = sampleSizes[MathUtils.RANDOM.nextInt(sampleSizes.length)];
        double phi = phis[MathUtils.RANDOM.nextInt(phis.length)];
        double miss = misses[MathUtils.RANDOM.nextInt(misses.length)];


        Logger.info("Worker #%d, Test #%d begin: %d\t%d\t%d\t%.1f\t%.1f", id, test, itemSetSize, sampleSize, resampleSize, phi, miss);
        String line = String.format("%d\t%d\t%d\t%.1f\t%.1f", itemSetSize, sampleSize, resampleSize, phi, miss);


        ItemSet items = new ItemSet(itemSetSize);
        Ranking center = items.getRandomRanking();
        MallowsModel model = new MallowsModel(center, phi);
        RankingSample sample = MallowsUtils.sample(model, sampleSize);
        Filter.removeItems(sample, miss);


        // First Method: Triangle no row
        long start = System.currentTimeMillis();
        SampleTriangle st1 = new SampleTriangle(center, sample);
        RIMRSampler resampler1 = new RIMRSampler(st1);
        RankingSample resample1 = resampler1.generate(resampleSize);
        MallowsModel model1 = reconstructor.reconstruct(resample1, center);
        long time1 = System.currentTimeMillis() - start;
        line += String.format("\t%.5f\t%.1f", model1.getPhi(), 0.001 * time1);


        // First Method: Triangle by row
        start = System.currentTimeMillis();
        SampleTriangleByRow st2 = new SampleTriangleByRow(center, sample);
        RIMRSampler resampler2 = new RIMRSampler(st2);
        RankingSample resample2 = resampler2.generate(resampleSize);
        MallowsModel model2 = reconstructor.reconstruct(resample2, center);
        long time2 = System.currentTimeMillis() - start;
        line += String.format("\t%.5f\t%.1f", model2.getPhi(), 0.001 * time2);



        // Third Method: 200 Bootstraps, 1 completion
        int boots = 200;
        int completions = 1;
        double[] bootstraps = new double[boots];
        start = System.currentTimeMillis();
        for (int j = 0; j < bootstraps.length; j++) {
          SampleCompleter completer = new SampleCompleter(sample);
          RankingSample resample = completer.complete(completions);
          MallowsModel mallows = reconstructor.reconstruct(resample, center);
          bootstraps[j] = mallows.getPhi();
        }
        double phi3 = MathUtils.mean(bootstraps);
        double var3 = MathUtils.variance(bootstraps);
        long time3 = System.currentTimeMillis() - start;
        line += String.format("\t%.5f\t%.6f\t%.1f", phi3, var3, 0.001 * time3);

        Logger.info("Worker #%d, Test #%d end  : " + line, id, test);
        write(line);
      }
      
    }
  }
  
  
  public static void main(String[] args) throws IOException, InterruptedException {
    File outFile = new File(Config.RESULTS_FOLDER, "incomplete.components.tsv");
    IncompleteComponentsTest test = new IncompleteComponentsTest(outFile, 4);
    test.test();
  }
}
