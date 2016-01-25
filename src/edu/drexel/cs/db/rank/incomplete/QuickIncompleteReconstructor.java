package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import static edu.drexel.cs.db.rank.incomplete.QuickIncompleteAttributes.ATTRIBUTES;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.filter.SampleCompleter;
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

/** Like IncompleteReconstructor, but it only uses bootstrapping with completer */
@Deprecated
public class QuickIncompleteReconstructor implements MallowsReconstructor {

  private File arff;
  private M5P classifier;
  private int trains;
  private int boots = QuickIncompleteAttributes.BOOTSTRAPS;
  
  public QuickIncompleteReconstructor(File arff) throws Exception {    
    this(arff, 0);
  }  
  
  public QuickIncompleteReconstructor(File arff, int trains) throws Exception {    
    this.arff = arff;
    this.trains = trains;
  }
  
  /** Use temp file for training instances just for this particular case */
  public QuickIncompleteReconstructor(int trains) throws Exception {
    if (trains <= 1) throw new IllegalArgumentException("Number of train instances must be greater than zero");
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
    Logger.info("QuickIncompleteReconstructor: %d items, %d rankings, %.3f missing rate", sample.getItemSet().size(), sample.size(), IncompleteUtils.getMissingRate(sample));
    if (trains > 0) {
      QuickIncompleteGenerator generator = new QuickIncompleteGenerator(arff);
      generator.setBootstraps(boots);
      generator.generateParallel(sample, trains);
      classifier = null;
    }
    
    
    if (classifier == null) load();
    
    Instance instance = new DenseInstance(ATTRIBUTES.size()); 
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_ITEMS), sample.getItemSet().size());
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sample.size());
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_MISSING), IncompleteUtils.getMissingRate(sample));
      
    if (center == null) center = CenterReconstructor.reconstruct(sample);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();

    
    // Completer
    double[] bootstraps = new double[boots];
    for (int j = 0; j < bootstraps.length; j++) {
      SampleCompleter completer = new SampleCompleter(sample);
      Sample resample = completer.complete(1);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      bootstraps[j] = mallows.getPhi();
    }
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));

    System.out.println(instance);
    double regressionPhi = this.classifier.classifyInstance(instance);
    return new MallowsModel(center, regressionPhi);
  }
}
