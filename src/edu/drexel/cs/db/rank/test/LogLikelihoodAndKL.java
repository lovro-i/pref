package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.distance.KL;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.plot.ScatterPlot;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class LogLikelihoodAndKL {

  public static void plot(File in, File png) throws IOException {
    ScatterPlot plot = new ScatterPlot(in);
    plot.setXIndex(0);
    plot.setYIndex(1);
    plot.setTitle("Correlation between PPM Distance and Log Likelihood");
    plot.setXLabel("PPM Distance");
    plot.setYLabel("Log Likelihood");
    plot.plot(png);
  }
  
  public static void main1(String[] args) {
    ItemSet items = new ItemSet(10);
    double phi = 0.2;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);    
    RankingSample sample = MallowsUtils.sample(model, 5000);
    
    // System.out.println(model.getLogLikelihood(sample));
    
    double[] phis = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8 };
    for (double ph: phis) {
      MallowsModel m = new MallowsModel(items.getReferenceRanking(), ph);    
      Logger.info("%.2f\t%.2f", ph, m.getLogLikelihood(sample));
    }
    
  }
  
  public static void main(String[] args) throws IOException {
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File file = new File(folder, "Measure.correlation.ll.kl.csv");
    
    File png = new File(folder, "Measure.correlation.ll.kl.png");
    if (file.exists()) plot(file, png);
    
    PrintWriter out = FileUtils.append(file);
    
    int n = 20;
    ItemSet items = new ItemSet(n);
    int N = 10000;
    
    for (int i = 1; i <= 1000; i++) {
      Logger.info("Test #%d", i);
      double phi1 = 0.1 + 0.7 * Math.random();
      
      MallowsModel model1 = new MallowsModel(items.getRandomRanking(), phi1);
      RankingSample sample1 = MallowsUtils.sample(model1, N);
      
      double phi2 = 0.1 + 0.7 * Math.random();
      MallowsModel model2 = new MallowsModel(model1.getCenter(), phi2);
      RankingSample sample2 = MallowsUtils.sample(model2, N);
      
      double phi3 = phi2;
      MallowsModel model3 = new MallowsModel(items.getRandomRanking(), phi3);
      RankingSample sample3 = MallowsUtils.sample(model3, N);
      
      double phi4 = 0.1 + 0.7 * Math.random();
      MallowsModel model4 = new MallowsModel(items.getRandomRanking(), phi4);
      RankingSample sample4 = MallowsUtils.sample(model4, N);
      
      
      out.println(String.format("%.6f, %.6f", KL.divergence(sample1, sample2), model1.getLogLikelihood(sample2)));
      out.println(String.format("%.6f, %.6f", KL.divergence(sample1, sample3), model1.getLogLikelihood(sample3)));
      out.println(String.format("%.6f, %.6f", KL.divergence(sample1, sample4), model1.getLogLikelihood(sample4)));
      out.println(String.format("%.6f, %.6f", KL.divergence(sample2, sample3), model2.getLogLikelihood(sample3)));
      out.println(String.format("%.6f, %.6f", KL.divergence(sample2, sample4), model2.getLogLikelihood(sample4)));
      out.println(String.format("%.6f, %.6f", KL.divergence(sample3, sample4), model3.getLogLikelihood(sample4)));
      out.flush();
      
      if (i % 100 == 0) plot(file, png);
    }
    
    out.close();
    
    plot(file, png);
  }
}
