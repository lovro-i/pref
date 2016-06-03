package edu.drexel.cs.db.db4pref.plot;

import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.sampler.RIMRSampler;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.db4pref.triangle.MallowsTriangle;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class ScatterPlot {

  private File input;
  private String title = "Title";
  private String xLabel = "X Axis";
  private String yLabel = "Y Axis";
  private int xIndex = 0;
  private int yIndex = 0;
  
  public ScatterPlot(File input) {
    this.input = input;
  }
  
  public ScatterPlot setXIndex(int xi) {
    this.xIndex = xi;
    return this;
  }
  
  public ScatterPlot setYIndex(int yi) {
    this.yIndex = yi;
    return this;
  }
  
  public ScatterPlot setTitle(String title) {
    this.title = title;
    return this;
  }
  
  public ScatterPlot setXLabel(String label) {
    this.xLabel = label;
    return this;
  }
  
  public ScatterPlot setYLabel(String label) {
    this.yLabel = label;
    return this;
  }
  
  private double[] getValues(String line) {
    StringTokenizer tokenizer = new StringTokenizer(line, ", ;\t");
    List<Double> vals = new ArrayList<Double>();
    while (tokenizer.hasMoreTokens()) {
      double val = Double.parseDouble(tokenizer.nextToken());
      vals.add(val);
    }
    
    double[] values = new double[vals.size()];
    for (int i = 0; i < values.length; i++) {
      values[i] = vals.get(i);      
    }
    return values;
  }
  
  
  private XYSeriesCollection getData() throws IOException {
    XYSeriesCollection dataset = new XYSeriesCollection();
    XYSeries series = new XYSeries(title);
    dataset.addSeries(series);
    
    List<String> lines = FileUtils.readLines(input);
    for (String line: lines) {
      double[] values = getValues(line);
      double x = values[xIndex];
      double y = values[yIndex];
      if (Double.isFinite(x) && Double.isFinite(y)) series.add(x, y);
    }
    
    return dataset;
  }
  
  public void plot(File out) throws IOException {
    XYSeriesCollection dataset = getData();
    
    JFreeChart chart = ChartFactory.createScatterPlot(
            title, // chart title
            xLabel, // x axis label
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
//    if (range == null) ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(true);
//    else ((NumberAxis) plot.getRangeAxis()).setRange(-range, range); 
    
//    LogAxis logAxis = new LogAxis("Sample size");
//    logAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//    plot.setDomainAxis(logAxis);
    
    XYItemRenderer renderer = plot.getRenderer();
    for (int i=0; i<dataset.getSeriesCount(); i++) {
      renderer.setSeriesShape(i, new Ellipse2D.Double(-4, -4, 8.0, 8.0));      
    }
    renderer.setSeriesPaint(0, new Color(0, 0, 100, 180));
    renderer.setSeriesPaint(1, new Color(200, 0, 0, 200));
    renderer.setSeriesPaint(2, new Color(0, 100, 0, 200));
    
    ChartUtilities.saveChartAsPNG(out, chart, 1600, 900); 
  }
}
