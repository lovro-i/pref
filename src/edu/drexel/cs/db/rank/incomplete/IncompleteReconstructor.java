package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import static edu.drexel.cs.db.rank.incomplete.IncompleteAttributes.ATTRIBUTES;
import edu.drexel.cs.db.rank.ml.TrainUtils;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sample.SampleCompleter;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangleByRow;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import edu.drexel.cs.db.rank.util.SystemOut;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import weka.classifiers.trees.M5P;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class IncompleteReconstructor implements MallowsReconstructor {

  private File arff;
  private M5P classifier;
  private int trains;
  
  public IncompleteReconstructor(File arff) throws Exception {    
    this(arff, 0);
  }  
  
  /** Create reconstructor that first generates <code>trains</code> training samples
   * 
   * @param arff File with training samples
   * @param trains Number of train samples to generate, 0 for no training (if you already have it in arff)
   * @throws Exception 
   */
  public IncompleteReconstructor(File arff, int trains) throws Exception {    
    this.arff = arff;
    this.trains = trains;
    // load();
  }
  
  public void load() throws Exception {
    long start = System.currentTimeMillis();
    InputStream is = new FileInputStream(arff);
    ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);    
    Instances data = source.getDataSet();
    is.close();
    data.setClassIndex(data.numAttributes() - 1);    
    classifier = new M5P();
    classifier.setMinNumInstances(4);    
    classifier.buildClassifier(data);
    System.out.println(String.format("Incomplete regression classifier learnt in %d ms", System.currentTimeMillis() - start));
  }

  
  
  @Override
  public MallowsModel reconstruct(Sample sample) throws Exception {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }
  
  
  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) throws Exception {
    Logger.info("IncompleteReconstructor: %d elements, %d rankings, %.3f missing rate", sample.getElements().size(), sample.size(), IncompleteUtils.getMissingRate(sample));
    if (trains > 0) {
      IncompleteGenerator generator = new IncompleteGenerator(arff);
      generator.generateParallel(sample, trains);
      classifier = null;
    }
    
    
    if (classifier == null) load();
    
    int resampleSize = IncompleteAttributes.RESAMPLE_SIZE;
    
    Instance instance = new DenseInstance(ATTRIBUTES.size()); 
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_ELEMENTS), sample.getElements().size());
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sample.size());
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_RESAMPLE_SIZE), resampleSize);
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_MISSING), IncompleteUtils.getMissingRate(sample));
      
    if (center == null) center = CenterReconstructor.reconstruct(sample);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    
    // triangle no row
    {
      SampleTriangle st = new SampleTriangle(center, sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_NO_ROW), mallows.getPhi());
      center = mallows.getCenter();
    }


    // triangle by row
    {
      SampleTriangleByRow st = new SampleTriangleByRow(center, sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_BY_ROW), mallows.getPhi());
    }

    
    // Completer
    double[] bootstraps = new double[IncompleteAttributes.BOOTSTRAPS];
    for (int j = 0; j < bootstraps.length; j++) {
      SampleCompleter completer = new SampleCompleter(sample);
      Sample resample = completer.complete(1);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      bootstraps[j] = mallows.getPhi();
    }
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));

    System.out.println(instance);
    double regressionPhi = this.classifier.classifyInstance(instance);
    return new MallowsModel(center, regressionPhi);
  }
  
  public static void main(String[] args) throws Exception {
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    File arff = new File(folder, "incomplete.train.arff");
    
    IncompleteReconstructor reconstructor = new IncompleteReconstructor(arff);
    
    int n = 15;
    ElementSet elements = new ElementSet(n);    
    double phi = 0.32;
    double missing = 0.55;
    int sampleSize = 3000;
    
    MallowsModel original = new MallowsModel(elements.getRandomRanking(), phi);
    
    MallowsTriangle triangle = new MallowsTriangle(original);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    Filter.remove(sample, missing);
    
    //reconstructor.trainParallel(sample, 6);
    MallowsModel model = reconstructor.reconstruct(sample);
    System.out.println();
    System.out.println("     Original Mallows Model: " + original);
    System.out.println("Reconstructed Mallows Model: " + model);
  }
}
