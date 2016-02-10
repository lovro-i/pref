package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.loader.SampleLoader;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PPMDistance;
import edu.drexel.cs.db.rank.reconstruct.SmartReconstructor;
import edu.drexel.cs.db.rank.triangle.Expands;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;


public class MixtureTestRandom {

  public static void main(String[] args) throws Exception {    
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File log = new File(folder, "Mallows.mixture.results.txt");
    
    //generateSamples(new File(folder, "Samples"), 10);
    
    //testSample(folder, 9);
    testSample(folder, 6);
    //testSample(folder, 4);
    // testSamples(folder);
    
//    for (int i = 0; i < 100; i++) {
//      PrintWriter out = FileUtils.append(log);
//      randomTest(out);    
//      out.close();
//    }
  }


  public static void generateSample(File folder, int id) throws IOException {
    int n = MathUtils.RANDOM.nextInt(20) + 10;
    ItemSet items = new ItemSet(n);
    MallowsMixtureModel model = new MallowsMixtureModel(items);
    
    int maxModels = 6;
    int models = MathUtils.RANDOM.nextInt(maxModels-1) + 2;
    for (int i = 0; i < models; i++) {
      Ranking center = items.getRandomRanking();
      double phi = MathUtils.RANDOM.nextDouble() * 0.8 + 0.05;
      double weight = MathUtils.RANDOM.nextDouble() + 0.05;
      MallowsModel mm = new MallowsModel(center, phi);
      model.add(mm, weight);
    }
    
    
    MallowsMixtureSampler sampler = new MallowsMixtureSampler(model);
    int sampleSize = (MathUtils.RANDOM.nextInt(95) + 5) * 100;
    // sampleSize = 2000;
    Sample sample = sampler.generate(sampleSize);
    
    double missing = Math.max(0, MathUtils.RANDOM.nextDouble() - 0.3); 
    // missing = 0.5;
    missing = 0;
    if (missing > 0) Filter.remove(sample, missing);

    
    sample.save(new File(folder, "sample."+id+".tsv"));
    PrintWriter out = FileUtils.write(new File(folder, "sample."+id+".txt"));
    out.println(String.format("%d items, %d models, %d rankings in sample, %.1f%% missing items\n", n, models, sampleSize, missing * 100));
    out.println(model);
    out.close();
  }
  
  public static void generateSamples(File folder, int count) throws IOException {
    for (int i = 1; i <= count; i++) {
      generateSample(folder, i);
    }
  }
  
  public static void randomTest(PrintWriter out) throws Exception {
    int n = MathUtils.RANDOM.nextInt(10) + 10;
    // n = 20;
    ItemSet items = new ItemSet(n);
    MallowsMixtureModel model = new MallowsMixtureModel(items);
    
    int maxModels = 6;
    int models = MathUtils.RANDOM.nextInt(maxModels) + 1;
    for (int i = 0; i < models; i++) {
      Ranking center = items.getRandomRanking();
      double phi = MathUtils.RANDOM.nextDouble() * 0.8 + 0.05;
      double weight = MathUtils.RANDOM.nextDouble() + 0.05;
      MallowsModel mm = new MallowsModel(center, phi);
      model.add(mm, weight);
    }
    
    
    MallowsMixtureSampler sampler = new MallowsMixtureSampler(model);
    int sampleSize = (MathUtils.RANDOM.nextInt(95) + 5) * 50;
    // sampleSize = 2000;
    Sample sample = sampler.generate(sampleSize);
    
    double missing = Math.max(0, MathUtils.RANDOM.nextDouble() - 0.3); 
    // missing = 0.5;
    missing = 0;
    if (missing > 0) Filter.remove(sample, missing);
    
    for (int i = 0; i < 300; i++) System.out.print('=');    
    System.out.println("\n");
    Logger.info("-----[ Test ]----------------------------");
    Logger.info("%d items, %d models, %d rankings in sample, %.1f%% missing items\n", n, models, sampleSize, missing * 100);
    Logger.info("-----[ Original ]------------------------");
    Logger.info(model);
    
    Expands.setThreshold(0.001);
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File arff = new File(folder, "incomplete.train.arff");
    SmartReconstructor single = new SmartReconstructor(arff, 4);
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single);
    long start = System.currentTimeMillis();
    MallowsMixtureModel rec = reconstructor.reconstruct(sample);
    double time = 0.001 * (System.currentTimeMillis() - start);
    
    double distance = PPMDistance.distance(sample, MallowsUtils.sample(rec, 10000));
    
    Logger.info("-----[ Test ]----------------------------");
    Logger.info("%d items, %d models, %d rankings in sample, %.1f%% missing items", n, models, sampleSize, missing * 100);
    Logger.info("Model distance: %.4f | Reconstructed in %.1f sec\n", distance, time);
    Logger.info("-----[ Original ]------------------------");
    Logger.info(model);
    Logger.info("-----[ Reconstructed ]-------------------");
    Logger.info(rec);
    
    out.println("\n\n-----[ Test ]----------------------------");
    out.println(String.format("%d items, %d models, %d rankings in sample, %.1f%% missing items", n, models, sampleSize, missing * 100));
    out.println(String.format("Model distance: %.4f | Reconstructed in %.1f sec\n", distance, time));    
    out.println("-----[ Original ]------------------------");
    out.println(model);
    out.println("-----[ Reconstructed ]-------------------");
    out.println(rec);
  }
  
  
  private static Sample loadSample(File folder, int id) throws IOException {
    File file = new File(folder, "sample." + id + ".tsv");
    if (!file.exists()) return null;
    Sample sample = new SampleLoader(true, false, false).loadSample(file);
    return sample;
  }
  
  private static String loadSampleInfo(File folder, int id) throws IOException {
    File txt = new File(folder, "sample."+id+".txt");
    return FileUtils.read(txt);
  }
  
  public static double testSample(File folder, int id) throws Exception {
    File samples = new File(folder, "Samples");
    Sample sample = loadSample(samples, id);
    String info = loadSampleInfo(samples, id);

    Logger.info("=====[ Test %d ]==============================================================================================================", id);
    Logger.info(info);


    File arff = new File(folder, "incomplete.train.arff");
    SmartReconstructor single = new SmartReconstructor(arff, 4);
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single);
    long start = System.currentTimeMillis();
    MallowsMixtureModel rec = reconstructor.reconstruct(sample);
    rec.getItemSet().letters();
    double time = 0.001 * (System.currentTimeMillis() - start);

    
    MallowsMixtureCompactor compactor = new MallowsMixtureCompactor(0.05, 0.05);
    
    
    double distance = PPMDistance.distance(sample, MallowsUtils.sample(rec, 10000));
    Logger.info("-----[ Original ]-------------------");
    Logger.info(info);
    Logger.info("-----[ Reconstructed ]-------------------");
    Logger.info(rec);
    Logger.info("Model distance: %.4f | Reconstructed in %.1f sec\n", distance, time);
    
    
    Logger.info("-----[ Compacted ]-------------------");
    MallowsMixtureModel compact = compactor.compact(rec);
    Logger.info(compact);
    double distanceComp = PPMDistance.distance(sample, MallowsUtils.sample(compact, 10000));
    Logger.info("Model distance: %.4f", distanceComp, time);
    
    return distance;
  }
  
  
  public static void testSamples(File folder) throws Exception {           
    double sumd = 0;
    int tests = 10;
    Expands.setThreshold(0.001);    
    double dists[] = new double[tests];
    for (int id = 1; id <= tests; id++) {
      dists[id-1] = testSample(folder, id);
      sumd += dists[id-1];
    }         
    
    for (int i = 0; i < dists.length; i++) {
      Logger.info("Test %d: %.4f", i+1, dists[i]);      
    }
    Logger.info("Mean model distance: " + (sumd / tests));
  }
  
}
