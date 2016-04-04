package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.distance.RankingDistance;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import jdistlib.disttest.NormalityTest;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Reconstruct1 {

  private static Random random = new SecureRandom();
  
  public static void whiskers() throws IOException {
    int n = 10;
    ItemSet items = new ItemSet(n);
    Ranking center = items.getReferenceRanking();
    RankingDistance dist = new KendallTauDistance();
    int sampleSize = 1000;
    int tests = 500;
    int models = 5;
    double[] perts = { 0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6};
    File folder = new File("C:\\Projects\\Rank\\Results");
    
    for (double phi=0.5; phi<=0.8; phi += 0.1) {
      File out = new File(folder, "values-combined-phi-"+phi+".csv");
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(out)));
      MallowsTriangle triangle = new MallowsTriangle(center, phi);
      RIMRSampler sampler = new RIMRSampler(triangle);
      
      for (double pert: perts) {
        System.out.println("Tests for phi = " + phi + ", pert = "+ pert);
        
        StringBuilder sb = new StringBuilder();
        for (int t = 0; t < tests; t++) {
          RankingSample sample = sampler.generate(sampleSize);

          MallowsModel model = new CompleteReconstructor().reconstruct(sample);
          int c = 1;
          double sum = model.getPhi();
          
          if (pert > 0.00001) {
            for (int j = 0; j < models; j++) {
              c++;
              RankingSample perturbed = perturb(sample, pert);
              MallowsModel m = new CompleteReconstructor().reconstruct(perturbed);
              sum += m.getPhi();
            }
          }
                    
          double rPhi = sum / c;
          if (t > 0) sb.append(",");
          sb.append(rPhi - phi);
        }
        
        writer.println(sb.toString());
        writer.flush();
      }
      
      writer.close();
    }
  }
  
  
  
  public static void main(String[] args) throws IOException, InterruptedException {
    // normalityTest();
    // errorDist();
    
    whiskers();
    
//    int n = 10;
//    
//    
//    File folder = new File("C:\\Projects\\Rank\\Results");
//    File out = new File(folder, "values.csv");
//    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(out)));
//    
//    testPhi(n, 100, 500, writer);
//    
////    double phi = 0.5;
////    double[] perts = { 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.5, 0.6};
////    //double[] perts = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
////    test(n, phi, 1000, 1000, writer, perts, 5);
//    writer.close();
  }
  
  private static Ranking perturb(Ranking r, double p) {
    Ranking r1 = new Ranking(r);
    for (int i = 0; i < r1.size()-1; i++) {
      double flip = random.nextDouble();
      if (flip < p) r1.swap(i, i+1);
    }
    return r1;
  }
  
  private static RankingSample perturb(RankingSample sample, double p) {
    RankingSample s = new RankingSample(sample.getItemSet());
    for (Ranking r: sample.rankings()) {
      Ranking r1 = perturb(r, p);
      s.add(r1);
    }
    return s;
  }
  
  public static void testPhi(int n, int samps, int tests, PrintWriter writer) throws IOException {    
    ItemSet items = new ItemSet(n);
    Ranking center = items.getReferenceRanking();
    RankingDistance dist = new KendallTauDistance();
    
    for (double phi = 0.025; phi < 1; phi += 0.025) {
      System.out.println("Phi "+phi);
      double sumPhi = 0;
      
      
      for (int t=0; t<tests; t++) {
        if ((t+1) % 100 == 0) System.out.println("Test #" + (t+1));
        MallowsTriangle triangle = new MallowsTriangle(center, phi);
        RIMRSampler sampler = new RIMRSampler(triangle);
        RankingSample sample = sampler.generate(samps);
        // Comb.comb(sample, 0.1);

        RankingSample perturbed = perturb(sample, 0.05);
        sample.addAll((Sample<? extends Ranking>) perturbed);

        MallowsModel m0 = new CompleteReconstructor().reconstruct(sample);
        // int centerDistance = (int) dist.distance(center, m0.getCenter());
        sumPhi += m0.getPhi();
      }

      writer.print(phi);
      writer.print(",");
      writer.println(sumPhi / tests);
      writer.flush();
    }
    
  }
  
  public static void test(int n, double phi, int samps, int tests, PrintWriter writer, double[] perts, int ns) throws IOException {    
    ItemSet items = new ItemSet(n);
    Ranking center = items.getReferenceRanking();
    RankingDistance dist = new KendallTauDistance();
    
    
    for (int t=0; t<tests; t++) {
      if ((t+1) % 100 == 0) System.out.println("Test #" + (t+1));
      MallowsTriangle triangle = new MallowsTriangle(center, phi);
      RIMRSampler sampler = new RIMRSampler(triangle);
      RankingSample sample = sampler.generate(samps);
      Filter.removeItems(sample, 0.1);
      
      StringBuilder sb = new StringBuilder();

      MallowsModel m0 = new CompleteReconstructor().reconstruct(sample);
      int centerDistance = (int) dist.distance(center, m0.getCenter());
      sb.append((phi - m0.getPhi()));
      
      
      for (int i = 0; i < perts.length; i++) {
        double pert = perts[i];
        double sum = m0.getE();
        for (int j = 0; j < ns; j++) {
          RankingSample perturbed = perturb(sample, pert);
          // perturbed.addAll(sample);
          MallowsModel model = new CompleteReconstructor().reconstruct(perturbed);
          
          sum += model.getE();
        }
        sum = sum / (ns+1);
        sum = MallowsModel.eToPhi(sum);
        sb.append(", ").append(phi-sum);
      }
      
      
                
      writer.println(sb.toString());
    }
    
  }
  
  private static double testCramerVonMises(double[] vals) {
    double s = NormalityTest.cramer_vonmises_statistic(vals);
    return NormalityTest.cramer_vonmises_pvalue(s, vals.length);
  }
  
  private static double testShapiroFrancia(double[] vals) {
    double s = NormalityTest.shapiro_francia_statistic(vals);
    return NormalityTest.shapiro_francia_pvalue(s, vals.length);
  }
  
  private static double testShapiroWilk(double[] vals) {
    double s = NormalityTest.shapiro_wilk_statistic(vals);
    return NormalityTest.shapiro_wilk_pvalue(s, vals.length);
  }
  
  private static double testKolmogorovLilliefors(double[] vals) {
    double s = NormalityTest.kolmogorov_lilliefors_statistic(vals);
    return NormalityTest.kolmogorov_lilliefors_pvalue(s, vals.length);
  }
  
  private static void normalityTest() {
    int n = 10000;
    double[] yes = new double[n];
    double[] no = new double[n];
    Random random = new Random();
    
    for (int i = 0; i < n; i++) {
      yes[i] = 60 + random.nextGaussian() * 15;
      // yes[i] = random.nextGaussian();
      //yes[i] = yes[i] / 60;
//      if (i % 2 == 0) no[i] = 60 + random.nextGaussian() * random.nextDouble() * 15;
//      else no[i] = 120 + random.nextGaussian() * random.nextDouble() * 30;
      no[i] = 50 + 20 * random.nextDouble();
    }
    
    Arrays.sort(yes);
    Arrays.sort(no);
    double sYes, sNo, pYes, pNo;
    
    sYes = NormalityTest.cramer_vonmises_statistic(yes);
    pYes = NormalityTest.cramer_vonmises_pvalue(sYes, n);
    sNo = NormalityTest.cramer_vonmises_statistic(no);
    pNo = NormalityTest.cramer_vonmises_pvalue(sNo, n);
    System.out.println("Cramer - Von Mises");
    System.out.println(String.format("sYes: %f; sNo: %f", sYes, sNo));
    System.out.println(String.format("pYes: %f; pNo: %f\n", pYes, pNo));

    // Ne radi
//    sYes = NormalityTest.dagostino_pearson_statistic(yes);
//    pYes = NormalityTest.dagostino_pearson_pvalue(sYes);
//    sNo = NormalityTest.dagostino_pearson_statistic(no);
//    pNo = NormalityTest.dagostino_pearson_pvalue(sNo);
//    System.out.println("Dagostino Pearson");
//    System.out.println(String.format("sYes: %f; sNo: %f", sYes, sNo));
//    System.out.println(String.format("pYes: %f; pNo: %f\n", pYes, pNo));
    
    sYes = NormalityTest.shapiro_francia_statistic(yes);
    pYes = NormalityTest.shapiro_francia_pvalue(sYes, n);
    sNo = NormalityTest.shapiro_francia_statistic(no);
    pNo = NormalityTest.shapiro_francia_pvalue(sNo, n);
    System.out.println("Shapiro Francia");
    System.out.println(String.format("sYes: %f; sNo: %f", sYes, sNo));
    System.out.println(String.format("pYes: %f; pNo: %f\n", pYes, pNo));
    
    sYes = NormalityTest.kolmogorov_lilliefors_statistic(yes);
    pYes = NormalityTest.kolmogorov_lilliefors_pvalue(sYes, n);
    sNo = NormalityTest.kolmogorov_lilliefors_statistic(no);
    pNo = NormalityTest.kolmogorov_lilliefors_pvalue(sNo, n);
    System.out.println("Kolmogorov Lilliefors");
    System.out.println(String.format("sYes: %f; sNo: %f", sYes, sNo));
    System.out.println(String.format("pYes: %f; pNo: %f\n", pYes, pNo));
    
    // Ne radi
//    sYes = NormalityTest.jarque_bera_statistic(yes);
//    pYes = NormalityTest.jarque_bera_pvalue(sYes);
//    sNo = NormalityTest.jarque_bera_statistic(no);
//    pNo = NormalityTest.jarque_bera_pvalue(sNo);
//    System.out.println("Jarque Bera");
//    System.out.println(String.format("sYes: %f; sNo: %f", sYes, sNo));
//    System.out.println(String.format("pYes: %f; pNo: %f\n", pYes, pNo));
    
    sYes = NormalityTest.shapiro_wilk_statistic(yes);
    pYes = NormalityTest.shapiro_wilk_pvalue(sYes, n);
    sNo = NormalityTest.shapiro_wilk_statistic(no);
    pNo = NormalityTest.shapiro_wilk_pvalue(sNo, n);
    System.out.println("Shapiro Wilk");
    System.out.println(String.format("sYes: %f; sNo: %f", sYes, sNo));
    System.out.println(String.format("pYes: %f; pNo: %f\n", pYes, pNo));
        
  }
  
   
  /** Graphs for reconstruction errors depending on sample size ***/
  public static void errorDist() throws IOException {
    int n = 10;
    File folder = new File("C:\\Projects\\Rank\\Results");
    ItemSet items = new ItemSet(n);
    Ranking center = items.getReferenceRanking();
    RankingDistance dist = new KendallTauDistance();

    int[] samples = { 50, 1000, 5000, 10000, 20000};
    double[] phis = { 0.3, 0.7, 0.9, 0.2, 0.4 };
    int tests = 1000;
    
    
    for (int i=0; i<samples.length; i++) {
      int ss = samples[i];
      double phi = phis[i];
      System.out.println("phi = " + phi + ", samples: "+ss);
      
      double[] valsAbs = new double[tests];
      double[] valsRel = new double[tests];

      for (int t=0; t<tests; t++) {
        if ((t+1) % 100 == 0) System.out.println("Test #" + (t+1));
        MallowsTriangle triangle = new MallowsTriangle(center, phi);
        RIMRSampler sampler = new RIMRSampler(triangle);
        for (int samps: samples) {        
          RankingSample sample = sampler.generate(samps);
          MallowsModel model = new CompleteReconstructor().reconstruct(sample);
          int centerDistance = (int) dist.distance(center, model.getCenter());          
          
          double absErr = phi - model.getPhi();
          double relErr = 100 * absErr / phi;
          valsAbs[t] = absErr;
          valsRel[t] = relErr;
        }
        
      }

      Arrays.sort(valsAbs);
      Arrays.sort(valsRel);
      
      double sumA=0, sumR=0;
      for (int j = 0; j < valsRel.length; j++) {
        sumA += valsAbs[j];
        sumR += valsRel[j];        
      }
      
      double meanA = sumA / n;
      double varianceA = 0;
      for (int j = 0; j < n; j++) {
        double d = valsAbs[j] - meanA;
        varianceA += d * d;
      }
      varianceA = varianceA / (n-1);
      
      System.out.println(String.format("avgA: %f, avgR: %f, varA: %f", (sumA / n), (sumR / n), varianceA));
      
      double pcvmr = Reconstruct1.testCramerVonMises(valsRel);
      double pklr = Reconstruct1.testKolmogorovLilliefors(valsRel);
      double psfr = Reconstruct1.testShapiroFrancia(valsRel);
      double pswr = Reconstruct1.testShapiroWilk(valsRel);
      
      double pcvma = Reconstruct1.testCramerVonMises(valsAbs);
      double pkla = Reconstruct1.testKolmogorovLilliefors(valsAbs);
      double psfa = Reconstruct1.testShapiroFrancia(valsAbs);
      double pswa = Reconstruct1.testShapiroWilk(valsAbs);
      
      System.out.println(String.format("Absolute: %d\t%f\t%f\t%f\t%f\t%f", ss, phi, pcvma, pkla, psfa, pswa));
      System.out.println(String.format("Relative: %d\t%f\t%f\t%f\t%f\t%f", ss, phi, pcvmr, pklr, psfr, pswr));
      
    }
    
    System.out.println("0 should mean that it is not normal, > 0 that it is");
  }
  
  /** Graphs for reconstruction errors depending on sample size ***/
  public static void errors() throws IOException {
    int n = 10;
    File folder = new File("C:\\Projects\\Rank\\Results");
    ItemSet items = new ItemSet(n);
    Ranking center = items.getReferenceRanking();
    RankingDistance dist = new KendallTauDistance();

    int[] samples = {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000};
    double[] phis = { 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 };
    int tests = 30;
    
    
    for (double phi: phis) {
      System.out.println("phi = " + phi);
      
      XYSeries phiRelSeries = new XYSeries("PhiRel");
      phiRelSeries.setDescription("Phi reconstruction relative error for phi = "+phi);
      XYSeries phiAbsSeries = new XYSeries("PhiAbs");
      phiAbsSeries.setDescription("Phi reconstruction absolute error for phi = "+phi);
      XYSeries centerSeries = new XYSeries("Center");
      centerSeries.setDescription("Center reconstruction error for phi = "+phi);

      for (int t=1; t<=tests; t++) {
        System.out.println("Test #" + t);
        MallowsTriangle triangle = new MallowsTriangle(center, phi);
        RIMRSampler sampler = new RIMRSampler(triangle);
        for (int samps: samples) {        
          RankingSample sample = sampler.generate(samps);
          MallowsModel model = new CompleteReconstructor().reconstruct(sample);
          int centerDistance = (int) dist.distance(center, model.getCenter());          
          
          double absErr = Math.abs(phi - model.getPhi());
          double relErr = 100 * absErr / phi;
          phiRelSeries.add(samps, relErr);
          phiAbsSeries.add(samps, absErr);
          centerSeries.add(samps, centerDistance);
          System.out.println(String.format("%f\t%d\t%d\t%f\t%f", phi, samps, centerDistance, model.getPhi(), relErr));
        }
        
        graph(new File(folder, "Error-rel-phi-"+phi+".png"), phiRelSeries, null, "Error [%]");
        graph(new File(folder, "Error-abs-phi-"+phi+".png"), phiAbsSeries, 1, "Error");
        graph(new File(folder, "Error-center-"+phi+".png"), centerSeries, null, "Error");
      }
      
      
    }
  }
  
  
  private static void graph(File file, XYSeries series, Integer max, String yLabel) throws IOException {
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(series);

    JFreeChart chart = ChartFactory.createScatterPlot(
            series.getDescription(), // chart title
            "Sample size", // x axis label
            yLabel, // y axis label
            dataset, // data 
            PlotOrientation.VERTICAL,
            false, // include legend
            true, // tooltips
            false // urls
            );    
    
    
    // Visual style
    chart.setBackgroundPaint(Color.WHITE); 
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);    
    if (max == null) ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(true);
    else ((NumberAxis) plot.getRangeAxis()).setRange(0, max); 
    
    LogAxis logAxis = new LogAxis("Sample size");
    logAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    plot.setDomainAxis(logAxis);
    
    XYItemRenderer renderer = plot.getRenderer();
    for (int i=0; i<dataset.getSeriesCount(); i++) {
      renderer.setSeriesShape(i, new Ellipse2D.Double(-4, -4, 8.0, 8.0));      
    }
    renderer.setSeriesPaint(0, new Color(0, 0, 100, 180));
    renderer.setSeriesPaint(1, new Color(200, 0, 0, 200));
    renderer.setSeriesPaint(2, new Color(0, 100, 0, 200));
    
    ChartUtilities.saveChartAsPNG(file, chart, 1600, 900); 
  }
  
}
