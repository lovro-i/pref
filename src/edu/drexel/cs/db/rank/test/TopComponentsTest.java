package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.top.TopSampleCompleter;
import edu.drexel.cs.db.rank.top.TopSampleTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangleByRow;
import edu.drexel.cs.db.rank.util.Config;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TopComponentsTest {

  private final PrintWriter out;
  private final int threads;
  
  private final int[] itemSetSizes = { 10, 15, 20 };
  private final double[] phis = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 };
  private final double[] tops = { 0, 0.1, 0.2, 0.5, 0.7 };
  private final int[] sampleSizes = { 1000, 2000, 5000, 10000 };
  private final int resampleSize = 50000;
  private final PolynomialReconstructor reconstructor = new PolynomialReconstructor();
  
  private TopComponentsTest(File outFile, int threads) throws IOException {
    this.threads = threads;
    
    boolean header = !outFile.exists();
    out = FileUtils.append(outFile);
    if (header) {
      out.println("# TopComponentsTest");
      out.println("# items, sampleSize, resampleSize, phi, top, min_length, phi_triangle, time_triangle (sec), phi_triangle_by_row, time_triangle_by_row (sec), phi_top_triangle, time_top_triangle (sec), phi_top_bootstrap_200, var_top_bootstrap_200, time_top_bootstrap_200 (sec)");
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
        double top = tops[MathUtils.RANDOM.nextInt(tops.length)];


        Logger.info("Worker #%d, Test #%d begin: %d\t%d\t%d\t%.1f\t%.1f", id, test, itemSetSize, sampleSize, resampleSize, phi, top);
        String line = String.format("%d\t%d\t%d\t%.1f\t%.1f", itemSetSize, sampleSize, resampleSize, phi, top);


        ItemSet items = new ItemSet(itemSetSize);
        Ranking center = items.getRandomRanking();
        MallowsModel model = new MallowsModel(center, phi);
        Sample sample = MallowsUtils.sample(model, sampleSize);
        int min = (int) (top * items.size());
        Filter.top(sample, min, items.size());
        line += String.format("\t%d", min);


        // First Method: Triangle no row
        long start = System.currentTimeMillis();
        SampleTriangle st1 = new SampleTriangle(center, sample);
        RIMRSampler resampler1 = new RIMRSampler(st1);
        Sample resample1 = resampler1.generate(resampleSize);
        MallowsModel model1 = reconstructor.reconstruct(resample1, center);
        long time1 = System.currentTimeMillis() - start;
        line += String.format("\t%.5f\t%.1f", model1.getPhi(), 0.001 * time1);


        // Second Method: Triangle by row
        start = System.currentTimeMillis();
        SampleTriangleByRow st2 = new SampleTriangleByRow(center, sample);
        RIMRSampler resampler2 = new RIMRSampler(st2);
        Sample resample2 = resampler2.generate(resampleSize);
        MallowsModel model2 = reconstructor.reconstruct(resample2, center);
        long time2 = System.currentTimeMillis() - start;
        line += String.format("\t%.5f\t%.1f", model2.getPhi(), 0.001 * time2);

        
        // Third Method: TopTriangle
        start = System.currentTimeMillis();
        TopSampleTriangle st3 = new TopSampleTriangle(center, sample);
        RIMRSampler resampler3 = new RIMRSampler(st3);
        Sample resample3 = resampler3.generate(resampleSize);
        MallowsModel model3 = reconstructor.reconstruct(resample3, center);
        long time3 = System.currentTimeMillis() - start;
        line += String.format("\t%.5f\t%.1f", model3.getPhi(), 0.001 * time3);

        
        // Fourth Method: 200 Bootstraps, 1 completion
        int boots = 200;
        int completions = 1;
        double[] bootstraps = new double[boots];
        start = System.currentTimeMillis();
        for (int j = 0; j < bootstraps.length; j++) {
          TopSampleCompleter completer = new TopSampleCompleter(sample);
          Sample resample = completer.complete(completions);
          MallowsModel mallows = reconstructor.reconstruct(resample, center);
          bootstraps[j] = mallows.getPhi();
        }
        double phi4 = MathUtils.mean(bootstraps);
        double var4 = MathUtils.variance(bootstraps);
        long time4 = System.currentTimeMillis() - start;
        line += String.format("\t%.5f\t%.6f\t%.1f", phi4, var4, 0.001 * time4);

        Logger.info("Worker #%d, Test #%d end  : " + line, id, test);
        write(line);
      }
      
    }
  }
  
  
  public static void main(String[] args) throws IOException, InterruptedException {
    File outFile = new File(Config.RESULTS_FOLDER, "top.components.tsv");
    TopComponentsTest test = new TopComponentsTest(outFile, 4);
    test.test();
  }
}
