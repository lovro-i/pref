package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.distance.RankingDistance;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.generator.Resampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class BootstrapCheck {
  
  /** Graphs for reconstruction errors depending on sample size ***/
  public static void go(int bootstraps, double threshold) throws IOException {
    int n = 10;
    File folder = new File("C:\\Projects\\Rank\\Results2");
    ItemSet items = new ItemSet(n);
    Ranking center = items.getReferenceRanking();
    RankingDistance dist = new KendallTauDistance();

    int[] samples = {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000};
    double[] phis = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9 };
    int tests = 30;
    
    for (int samps: samples) {   
    
      System.out.println("Samples "+samps);
      XYSeries seriesOK = new XYSeries("Good");
      seriesOK.setDescription("Phi reconstruction for sample size "+samps);
      XYSeries seriesBad = new XYSeries("Bad");
      

      for (int t=1; t<=tests; t++) {
        
        for (double phi: phis) {
          MallowsTriangle triangle = new MallowsTriangle(center, phi);
          RIMRSampler sampler = new RIMRSampler(triangle);
          Sample sample = sampler.generate(samps);
          
          // No Bootstrap
          MallowsModel model = new CompleteReconstructor().reconstruct(sample);
          int centerDistance = (int) dist.distance(center, model.getCenter()); 

          
          // Bootstrap
          Resampler resampler = new Resampler(sample);          
          double phim = 0;
          for (int i = 0; i < bootstraps; i++) {
            Sample resample = resampler.resample();
            MallowsModel m = new CompleteReconstructor().reconstruct(resample);
            phim += m.getPhi();
          }
          phim = phim / bootstraps;
          
          double diff = Math.abs(model.getPhi() - phim);          
          if (diff < threshold) seriesOK.add(phi, model.getPhi());
          else seriesBad.add(phi, model.getPhi());
        }

      }
      graph(new File(folder, "Reconstruct.samples."+samps+".bootstraps."+bootstraps+".threshold."+threshold+".png"), seriesOK, seriesBad, 1);
    }
  }
  
  
  private static void graph(File file, XYSeries series1, XYSeries series2, Integer range) throws IOException {
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(series1);
    dataset.addSeries(series2);

    JFreeChart chart = ChartFactory.createScatterPlot(
            series1.getDescription(), // chart title
            "Original phi", // x axis label
            "Reconstructed phi", // y axis label
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
    else ((NumberAxis) plot.getRangeAxis()).setRange(0, range); 
    
    // LogAxis logAxis = new LogAxis("Sample size");
    // logAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    // plot.setDomainAxis(logAxis);
    
    XYItemRenderer renderer = plot.getRenderer();
    for (int i=0; i<dataset.getSeriesCount(); i++) {
      renderer.setSeriesShape(i, new Ellipse2D.Double(-4, -4, 8.0, 8.0));      
    }
    renderer.setSeriesPaint(0, new Color(0, 0, 100, 180));
    renderer.setSeriesPaint(1, new Color(100, 0, 0, 200));
    renderer.setSeriesPaint(2, new Color(0, 100, 0, 200));
    
    ChartUtilities.saveChartAsPNG(file, chart, 1600, 900); 
  }

  
  public static Thread run(int bootstraps, double threshold) {
    
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          go(bootstraps, threshold);
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    };
    
    t.start();
    return t;
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    List<Thread> threads = new ArrayList<Thread>();
    threads.add(run(10, 0.1));
    threads.add(run(10, 0.05));
    threads.add(run(100, 0.1));
    threads.add(run(100, 0.05));
    
    for (Thread t: threads) t.join();
  }
}
