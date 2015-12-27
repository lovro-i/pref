package com.rankst.mixture;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.filter.Filter;
import com.rankst.model.MallowsModel;
import com.rankst.util.FileUtils;
import com.rankst.util.Logger;
import com.rankst.util.MathUtils;
import java.io.File;
import java.io.PrintWriter;


public class MixtureTestRandom {

  public static void main(String[] args) throws Exception {    
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    File log = new File(folder, "Mallows.mixture.results.txt");
    
    for (int i = 0; i < 100; i++) {
      PrintWriter out = FileUtils.append(log);
      randomTest(out);    
      out.close();
    }
  }
  
  
  public static void randomTest(PrintWriter out) throws Exception {
    int n = MathUtils.RANDOM.nextInt(20) + 10;
    // n = 20;
    ElementSet elements = new ElementSet(n);
    MallowsMixtureModel model = new MallowsMixtureModel(elements);
    
    int maxModels = 6;
    int models = MathUtils.RANDOM.nextInt(maxModels) + 1;
    for (int i = 0; i < models; i++) {
      Ranking center = elements.getRandomRanking();
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
    if (missing > 0) Filter.remove(sample, missing);
    
    Logger.info("-----[ Test ]----------------------------");
    Logger.info("%d elements, %d models, %d rankings in sample, %.1f%% missing elements\n", n, models, sampleSize, missing * 100);
    Logger.info("-----[ Original ]------------------------");
    Logger.info(model);
    
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    File arff = new File(folder, "incomplete.train.arff");
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(arff, 1);
    long start = System.currentTimeMillis();
    MallowsMixtureModel rec = reconstructor.reconstruct(sample);
    double time = 0.001 * (System.currentTimeMillis() - start);
    
    Logger.info("-----[ Test ]----------------------------");
    Logger.info("%d elements, %d models, %d rankings in sample, %.1f%% missing elements; reconstructed in %.1f sec\n", n, models, sampleSize, missing * 100, time);
    Logger.info("-----[ Original ]------------------------");
    Logger.info(model);
    Logger.info("-----[ Reconstructed ]-------------------");
    Logger.info(rec);
    
    out.println("\n\n-----[ Test ]----------------------------");
    out.println(String.format("%d elements, %d models, %d rankings in sample, %.1f%% missing elements; reconstructed in %.1f sec\n", n, models, sampleSize, missing * 100, time));
    out.println("-----[ Original ]------------------------");
    out.println(model);
    out.println("-----[ Reconstructed ]-------------------");
    out.println(rec);
  }
}
