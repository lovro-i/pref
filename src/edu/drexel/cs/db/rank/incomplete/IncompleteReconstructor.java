package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import static edu.drexel.cs.db.rank.incomplete.IncompleteAttributes.ATTRIBUTES;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.filter.SampleCompleter;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangleByRow;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import weka.classifiers.trees.M5P;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


@Deprecated
public class IncompleteReconstructor implements MallowsReconstructor {

  private File arff;
  private M5P classifier;
  private int trains;
  private int boots = IncompleteAttributes.BOOTSTRAPS;
  
  /** Use arff file without training new examples */
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
  }
  
  /** Use temp file for training instances just for this particular case */
  public IncompleteReconstructor(int trains) throws Exception {
    if (trains < 1) throw new IllegalArgumentException("Number of train instances must be greater than zero");
    this.arff = File.createTempFile("train.", ".arff");
    this.arff.deleteOnExit();
    this.trains = trains;
  }
  
  
  public void setBootstraps(int bootstraps) {
    this.boots = bootstraps;
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
    Logger.info("IncompleteReconstructor: %d items, %d rankings, %.3f missing rate", sample.getItemSet().size(), sample.size(), IncompleteUtils.getMissingRate(sample));
    if (trains > 0) {
      IncompleteGenerator generator = new IncompleteGenerator(arff);
      generator.setBootstraps(boots);
      generator.generateParallel(sample, trains);
      classifier = null;
    }
    
    
    if (classifier == null) load();
    
    int resampleSize = IncompleteAttributes.RESAMPLE_SIZE;
    
    Instance instance = new DenseInstance(ATTRIBUTES.size()); 
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_ITEMS), sample.getItemSet().size());
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
    double[] bootstraps = new double[boots];
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
    
    
    int n = 25;
    ItemSet items = new ItemSet(n);    
    double phi = 0.32;
    double missing = 0.55;
    int sampleSize = 3000;
    
    MallowsModel original = new MallowsModel(items.getRandomRanking(), phi);
    
    MallowsTriangle triangle = new MallowsTriangle(original);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    Filter.remove(sample, missing);
    
    IncompleteReconstructor reconstructor = new IncompleteReconstructor(2);
    reconstructor.setBootstraps(5);
    MallowsModel model = reconstructor.reconstruct(sample);
    System.out.println();
    System.out.println("     Original Mallows Model: " + original);
    System.out.println("Reconstructed Mallows Model: " + model);
  }
}
