package com.rankst.ml;

import com.rankst.measure.LinearError;
import com.rankst.measure.SquareError;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/** Measure error for different reconstructions */
public class Train3 {

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
  
  public Train3(int n, File folder) {
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
  
  private void testModel(AbstractClassifier regressionModel, int samples, double phiStep, int perPhi) throws Exception {
    testModel(regressionModel, samples, phis(phiStep), perPhi);
  }

  private void testModel(AbstractClassifier regressionModel, int samples, double phis[], int perPhi) throws Exception {
    int size = phis.length * perPhi;
    System.out.println("Creating " + size + " samples");
    
    
    LinearError directLinearError = new LinearError();
    LinearError bootstrapLinearError = new LinearError();
    LinearError regressionLinearError = new LinearError();
    
    SquareError directSquareError = new SquareError();
    SquareError bootstrapSquareError = new SquareError();
    SquareError regressionSquareError = new SquareError();
    
    
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

          
        // Regression
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
        
        directLinearError.add(phi, model.getPhi());
        bootstrapLinearError.add(phi, phib);
        regressionLinearError.add(phi, smartPhi);
        
        directSquareError.add(phi, model.getPhi());
        bootstrapSquareError.add(phi, phib);
        regressionSquareError.add(phi, smartPhi);
      }      
    }
    
    System.out.println(String.format("Mean linear absolute error. Direct: %f, Bootstrap: %f, Regression: %f", directLinearError.getError(), bootstrapLinearError.getError(), regressionLinearError.getError()));
    System.out.println(String.format("Mean squared error error. Direct: %f, Bootstrap: %f, Regression: %f", directSquareError.getError(), bootstrapSquareError.getError(), regressionSquareError.getError()));
  }
  
  private AbstractClassifier trainModel(Instances data) throws Exception {
    data.setClassIndex(data.numAttributes() - 1);
    
    M5P model = new M5P();
    model.setMinNumInstances(4);
    
    model.buildClassifier(data); 
    System.out.println(model);
    return model;
  }
  
  public static void main(String[] args) throws Exception {
    File folder = new File("D:\\Projects\\Rankst\\Results2");    
    long start = System.currentTimeMillis();
    Train3 test = new Train3(10, folder);
    
    InputStream is = new FileInputStream(new File(folder, "train.arff"));
    ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
    Instances data = source.getDataSet();
    System.out.println("Loaded instances: "+data.size());
    
    AbstractClassifier model = test.trainModel(data);
    test.testModel(model, 2000, 0.05, 20);
  }
  
}
