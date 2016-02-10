package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.util.TrainUtils;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.util.FileUtils;
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
  
  
  private XYSeriesCollection graphData(int n, double[] phis, int[] sampleSizes) throws IOException {    
    String name = String.format("precision.%d.csv", n);    
    List<String> lines = FileUtils.readLines(new File(folder, name));
    
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
    String title = "Reconstruction precision for n = " + n + " items";
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
    File temp = File.createTempFile("train.", ".arff");
    System.out.println(temp);
  }
  
}
