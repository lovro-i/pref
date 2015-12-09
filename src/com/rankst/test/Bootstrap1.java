package com.rankst.test;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.distance.RankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.generator.Resampler;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.DirectReconstructor;
import com.rankst.reconstruct.MallowsReconstructor;
import com.rankst.triangle.MallowsTriangle;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
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


public class Bootstrap1 {
  
  /** Graphs for reconstruction errors depending on sample size ***/
  public static void errors() throws IOException {
    int n = 10;
    File folder = new File("D:\\Projects\\Rankst\\Results2");
    ElementSet elements = new ElementSet(n);
    Ranking center = elements.getReferenceRanking();
    RankingDistance dist = new KendallTauRankingDistance();

    int[] samples = {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000};
    double[] phis = { 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 };
    int tests = 30;
    int bootstraps = 10;
    
    for (double phi: phis) {
      System.out.println("phi = " + phi);
      
      XYSeries phiAbsSeries = new XYSeries("No bootstrap");
      phiAbsSeries.setDescription("Phi reconstruction absolute error for phi = "+phi);

      XYSeries phiAbsBootSeries = new XYSeries("Bootstrap x"+bootstraps);
      phiAbsBootSeries.setDescription("Phi reconstruction absolute error for phi = "+phi+" with bootstrap x"+bootstraps);
      
      XYSeries phiRelSeries = new XYSeries("PhiRel");
      phiRelSeries.setDescription("Phi reconstruction relative error for phi = "+phi);
      
      XYSeries centerSeries = new XYSeries("Center");
      centerSeries.setDescription("Center reconstruction error for phi = "+phi);

      for (int t=1; t<=tests; t++) {
        System.out.println("Test #" + t + ", phi "+phi);
        MallowsTriangle triangle = new MallowsTriangle(center, phi);
        RIMRSampler sampler = new RIMRSampler(triangle);
        for (int samps: samples) {        
          Sample sample = sampler.generate(samps);
          MallowsModel model = new DirectReconstructor().reconstruct(sample);
          int centerDistance = (int) dist.distance(center, model.getCenter());          
          
          double absErr = phi - model.getPhi();
          double relErr = 100 * absErr / phi;
          phiRelSeries.add(samps, relErr);
          phiAbsSeries.add(samps / 1.05, absErr);
          centerSeries.add(samps, centerDistance);
          System.out.println(String.format("%f\t%d\t%d\t%f\t%f", phi, samps, centerDistance, model.getPhi(), relErr));
          
          // Bootstrap
          Resampler resampler = new Resampler(sample);          
          //double phim = 0;
          for (int i = 0; i < bootstraps; i++) {
            Sample resample = resampler.resample();
            MallowsModel m = new DirectReconstructor().reconstruct(resample);
            double absErrBoot = phi - m.getPhi();
            phiAbsBootSeries.add(1.05d * samps, absErrBoot);
            //a/ phim += m.getPhi();
          }
          //a/ phim = phim / bootstraps;
          //a/ double absErrBoot = phi - phim
          //a/ phiAbsBootSeries.add(1.05d * samps, absErrBoot);
          
        }
        
        graph(new File(folder, "Error-abs-phi-"+phi+".png"), phiAbsSeries, phiAbsBootSeries, 1, "Error");
        // graph(new File(folder, "Error-rel-phi-"+phi+".png"), phiRelSeries, null, "Error [%]");                
        // graph(new File(folder, "Error-center-"+phi+".png"), centerSeries, null, "Error");
      }
            
    }
  }
  
  
  private static void graph(File file, XYSeries seriesRef, XYSeries seriesBoot, Integer range, String yLabel) throws IOException {
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(seriesRef);
    dataset.addSeries(seriesBoot);

    JFreeChart chart = ChartFactory.createScatterPlot(
            seriesRef.getDescription(), // chart title
            "Sample size", // x axis label
            yLabel, // y axis label
            dataset, // data 
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
            );    
    
    
    // Visual style
    chart.setBackgroundPaint(Color.WHITE); 
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);    
    if (range == null) ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(true);
    else ((NumberAxis) plot.getRangeAxis()).setRange(-range, range); 
    
    LogAxis logAxis = new LogAxis("Sample size");
    logAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    plot.setDomainAxis(logAxis);
    
    XYItemRenderer renderer = plot.getRenderer();
    for (int i=0; i<dataset.getSeriesCount(); i++) {
      renderer.setSeriesShape(i, new Ellipse2D.Double(-4, -4, 8.0, 8.0));      
    }
    renderer.setSeriesPaint(0, new Color(0, 0, 100, 180));
    renderer.setSeriesPaint(1, new Color(100, 0, 0, 20));
    renderer.setSeriesPaint(2, new Color(0, 100, 0, 200));
    
    ChartUtilities.saveChartAsPNG(file, chart, 1600, 900); 
  }


  public static void main(String[] args) throws IOException {
    errors();
  }
}
