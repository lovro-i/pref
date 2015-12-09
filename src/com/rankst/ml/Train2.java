package com.rankst.ml;

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
import com.rankst.util.Utils;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/** Try with MultilayerPerceptron, M5P
 *  https://www.packtpub.com/books/content/regression-models-weka
 * Train2: Single model for all sample sizes
 * @author Lovro
 */
public class Train2 {
  
  public static final Attribute ATTRIBUTE_SAMPLES = new Attribute("samples");
  public static final Attribute ATTRIBUTE_DIRECT_PHI = new Attribute("direct_phi");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_PHI = new Attribute("bootstrap_phi");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_VAR = new Attribute("bootstrap_var");
  public static final Attribute ATTRIBUTE_REAL_PHI = new Attribute("real_phi");
  
  public static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();
  
  static {
    ATTRIBUTES.add(ATTRIBUTE_SAMPLES);
    ATTRIBUTES.add(ATTRIBUTE_DIRECT_PHI);
    ATTRIBUTES.add(ATTRIBUTE_BOOTSTRAP_PHI);
    ATTRIBUTES.add(ATTRIBUTE_BOOTSTRAP_VAR);
    ATTRIBUTES.add(ATTRIBUTE_REAL_PHI);
  }
  
  private int n;
  private int bootstraps = 10;

  private final File folder;
  private final ElementSet elements;
  private final Ranking center;
  private final RankingDistance dist = new KendallTauRankingDistance();
  
  public Train2(int n, File folder) {
    this.n = n;
    this.folder = folder;
    this.elements = new ElementSet(n);
    this.center = elements.getReferenceRanking();
  }
  
  private double[] phis(double phiStep) {
    List<Double> ps = new ArrayList<Double>();
    double p = phiStep;
    while (p < 1) {
      ps.add(p);
      p += phiStep;
    }
    
    double phis[] = new double[ps.size()];
    for (int i = 0; i < phis.length; i++) {
      phis[i] = ps.get(i);
    }
    
    return phis;
  }
  
  
  private Instances buildDataset(int[] samples, double phiStep, int perPhi) throws IOException {
    return buildDataset(samples, phis(phiStep), perPhi);
  }
    
  private Instances buildDataset(int[] samples, double phis[], int perPhi) throws IOException {
    int size = phis.length * perPhi;
    System.out.println("Creating " + size + " samples");
    Instances data = new Instances("Train", ATTRIBUTES, size);
    
    for (int samps: samples) {
      System.out.println("Samples: "+samps);

      for (Double phi: phis) {
        System.out.println("Phi: " + phi);
        for (int i = 0; i < perPhi; i++) {

          // Sample
          MallowsTriangle triangle = new MallowsTriangle(center, phi);
          RIMRSampler sampler = new RIMRSampler(triangle);
          Sample sample = sampler.generate(samps);

          // No Bootstrap
          MallowsModel model = new DirectReconstructor().reconstruct(sample);


          // Bootstrap
          Resampler resampler = new Resampler(sample);          
          double phib = 0;
          double boots[] = new double[bootstraps];
          for (int j = 0; j < bootstraps; j++) {
            Sample resample = resampler.resample();
            MallowsModel m = new DirectReconstructor().reconstruct(resample);
            phib += m.getPhi();
            boots[j] = m.getPhi();
          }
          phib = phib / bootstraps;


          Instance instance = new DenseInstance(ATTRIBUTES.size());
          instance.setValue(ATTRIBUTE_SAMPLES, samps);
          instance.setValue(ATTRIBUTE_DIRECT_PHI, model.getPhi());
          instance.setValue(ATTRIBUTE_BOOTSTRAP_PHI, phib);
          instance.setValue(ATTRIBUTE_BOOTSTRAP_VAR, Utils.variance(boots));
          instance.setValue(ATTRIBUTE_REAL_PHI, phi);
          instance.setDataset(data);
          data.add(instance);
        }
      }
    
      BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folder, "train.arff")));
      writer.write(data.toString());
      writer.close();
    
    }
    
    return data;
  }
  



  private AbstractClassifier trainModel(Instances data) throws Exception {
    data.setClassIndex(data.numAttributes() - 1);
    
    M5P model = new M5P();
    model.setMinNumInstances(4);
    
    model.buildClassifier(data); 
    System.out.println(model);
    return model;
  }
    
  
  private XYSeriesCollection testModel(AbstractClassifier regressionModel, int samples, double phiStep, int perPhi) throws Exception {
    return testModel(regressionModel, samples, phis(phiStep), perPhi);
  }

  private XYSeriesCollection testModel(AbstractClassifier regressionModel, int samples, double phis[], int perPhi) throws Exception {
    int size = phis.length * perPhi;
    System.out.println("Creating " + size + " samples");
    
    XYSeries series1 = new XYSeries("No Bootstrap");
    XYSeries series2 = new XYSeries("Bootstrap");
    XYSeries series3 = new XYSeries("Regression");
    
    for (Double phi: phis) {
      System.out.println("Test Phi: " + phi);
      for (int i = 0; i < perPhi; i++) {

        // Sample
        MallowsTriangle triangle = new MallowsTriangle(center, phi);
        RIMRSampler sampler = new RIMRSampler(triangle);
        Sample sample = sampler.generate(samples);
        
        // No Bootstrap
        MallowsModel model = new DirectReconstructor().reconstruct(sample);


        // Bootstrap
        Resampler resampler = new Resampler(sample);          
        double phib = 0;
        double boots[] = new double[bootstraps];
        for (int j = 0; j < bootstraps; j++) {
          Sample resample = resampler.resample();
          MallowsModel m = new DirectReconstructor().reconstruct(resample);
          phib += m.getPhi();
          boots[j] = m.getPhi();
        }
        phib = phib / bootstraps;

          
        Instance instance = new DenseInstance(ATTRIBUTES.size());
        instance.setValue(0, samples);
        instance.setValue(1, model.getPhi());
        instance.setValue(2, phib);
        instance.setValue(3, Utils.variance(boots));
        //instance.setValue(4, samples);
//        instance.setValue(ATTRIBUTE_SAMPLES, samples);
//        instance.setValue(ATTRIBUTE_DIRECT_PHI, model.getPhi());
//        instance.setValue(ATTRIBUTE_BOOTSTRAP_PHI, phib);
//        instance.setValue(ATTRIBUTE_BOOTSTRAP_VAR, Utils.variance(boots));
//        instance.setValue(ATTRIBUTE_REAL_PHI, phi);
        
        double smartPhi = regressionModel.classifyInstance(instance);
        
        series1.add((double) phi, model.getPhi());
        series2.add((double) phi, phib);
        series3.add((double) phi, smartPhi);
        
      }      
    }
    
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(series1);
    dataset.addSeries(series2);
    dataset.addSeries(series3);
    return dataset;
  }
  
  
  private void graph(File file, XYSeriesCollection dataset, String title) throws IOException {

    JFreeChart chart = ChartFactory.createScatterPlot(
            title, // chart title
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
    ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(true);
    
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

  

  
  public static void main(String[] args) throws Exception {
    File folder = new File("D:\\Projects\\Rankst\\Results2");    
    long start = System.currentTimeMillis();
    Train2 train = new Train2(10, folder);
    
    int[] samples = { 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000 };
    
    // Instances data = train.buildDataset(samples, 0.05, 100);
    
    InputStream is = new FileInputStream(new File(folder, "train.arff"));
    DataSource source = new DataSource(is);
    Instances data = source.getDataSet();
    System.out.println("Loaded instances: "+data.size());
    
    
    AbstractClassifier model = train.trainModel(data);
    XYSeriesCollection dataset = train.testModel(model, 2000, 0.05, 20);
    
    File file = new File(folder, model.getClass().getSimpleName()+"."+samples+".png");
    train.graph(file, dataset, "Reconstruction with regression");
    
    System.out.println(String.format("Done in %d ms", System.currentTimeMillis() - start));
  }
  
}
