package com.rankst.test;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.ml.RegressionReconstructor;
import com.rankst.ml.TrainUtils;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.CompleteReconstructor;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.util.FileUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.StringTokenizer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/** For a fixed n and fixed phi, change number of samples and watch the reconstruction precision */
public class ReconstructionPrecision {

  private final File folder;
  
  public ReconstructionPrecision(File folder) {
    this.folder = folder;
  }
  
    
  private void test(int n, double[] phis, int[] sampleSizes) throws Exception {
    ElementSet elements = new ElementSet(n);
    RegressionReconstructor reconstructor = new RegressionReconstructor(new File(folder, "complete.train.arff"), new CompleteReconstructor());
    
    String name = String.format("precision.%d.csv", n);
    PrintWriter out = FileUtils.append(new File(folder, name));
        
    for (double phi: phis) {
      System.out.println(String.format("Testing n = %d, phi = %2f", n, phi));
      for (int sampleSize: sampleSizes) {
        MallowsTriangle triangle = new MallowsTriangle(elements.getReferenceRanking(), phi);
        RIMRSampler sampler = new RIMRSampler(triangle);
        Sample sample = sampler.generate(sampleSize);
        MallowsModel mallows = reconstructor.reconstruct(sample);
        out.println(String.format("%.2f,%d,%.3f", phi, sampleSize, mallows.getPhi()));
      }
      out.flush();
    }
    out.close();
  }
  
  
  private double meanReconstructionPhi(List<String> lines, double phi, int sampleSize) {
    double sum = 0;
    int count = 0;
    double epsilon = 0.001;
    
    for (String line: lines) {
      StringTokenizer tokenizer = new StringTokenizer(line, ",");
      double originalPhi = Double.parseDouble(tokenizer.nextToken());
      if (Math.abs(phi - originalPhi) > epsilon) continue;
      int samples = Integer.parseInt(tokenizer.nextToken());
      if (samples != sampleSize) continue;
      double recPhi = Double.parseDouble(tokenizer.nextToken());
      sum += recPhi;
      count++;
    }
    return sum / count;
  }
  
  
  private XYSeriesCollection graphData(int n, double[] phis, int[] sampleSizes) throws FileNotFoundException {    
    String name = String.format("precision.%d.csv", n);    
    List<String> lines = FileUtils.read(new File(folder, name));
    
    XYSeriesCollection dataset = new XYSeriesCollection();
    
    XYSeries[] series = new XYSeries[phis.length];
    for (int i = 0; i < series.length; i++) {
      series[i] = new XYSeries(String.format("Phi: %.2f", phis[i]));      
      double phi = phis[i];
      
      for(int sampleSize: sampleSizes) {
        double mean = meanReconstructionPhi(lines, phi, sampleSize);
        series[i].add(sampleSize, mean);
      }
    
      dataset.addSeries(series[i]);
    }
    
    return dataset;
  }
  
  
  private void graph(int n, double[] phis, int[] sampleSizes) throws IOException {
    XYSeriesCollection dataset = graphData(n, phis, sampleSizes);
    graph(n, dataset);
  }
  
  private void graph(int n, XYSeriesCollection dataset) throws IOException {
    String title = "Reconstruction precision for n = " + n + " elements";
    JFreeChart chart = ChartFactory.createXYLineChart(title, "Sample size", "Reconstructed phi", dataset, PlotOrientation.VERTICAL, true, true, true);
    
    
    
    // Visual style
    chart.setBackgroundPaint(Color.WHITE); 
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);    
    // ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(true);
    
     LogAxis logAxis = new LogAxis("Sample size");
     logAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
     plot.setDomainAxis(logAxis);
    
     XYItemRenderer renderer = plot.getRenderer();
//    for (int i=0; i<dataset.getSeriesCount(); i++) {
//      renderer.setSeriesShape(i, new Ellipse2D.Double(-4, -4, 8.0, 8.0));      
//    }
//    renderer.setSeriesPaint(0, new Color(0, 0, 100, 180));
//    renderer.setSeriesPaint(1, new Color(100, 0, 0, 200));
//    renderer.setSeriesPaint(2, new Color(0, 100, 0, 200));

    for (int i = 0; i < dataset.getSeriesCount(); i++) {
      renderer.setSeriesStroke(i, new BasicStroke(3.5f));
    }



    File png = new File(folder, "precision."+n+".png");
    ChartUtilities.saveChartAsPNG(png, chart, 1600, 900); 
  }
  
  
  public static void main(String[] args) throws Exception {
    File folder = new File("C:\\Projects\\Rankst\\Results.3");    
    
    double[] phis = TrainUtils.step(0.05, 0.85, 0.05);
    int[] sampleSizes = { 100, 200, 500, 1000, 2000, 5000, 10000 };
    
    ReconstructionPrecision tester = new ReconstructionPrecision(folder);    
    
    int[] ns = { 15, 20, 25, 30, 40, 50, 70, 100 };
    int reps = 10;
    
    for (int n: ns) {
      for (int i = 0; i < reps; i++) {
        tester.test(n, phis, sampleSizes);
      }
      tester.graph(n, phis, sampleSizes);
    }
  }
  
}
