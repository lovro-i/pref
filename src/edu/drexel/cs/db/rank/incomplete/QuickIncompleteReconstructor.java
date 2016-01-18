package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import static edu.drexel.cs.db.rank.incomplete.QuickIncompleteAttributes.ATTRIBUTES;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.sample.SampleCompleter;
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


public class QuickIncompleteReconstructor implements MallowsReconstructor {

  private File arff;
  private M5P classifier;
  private int trains;
  
  public QuickIncompleteReconstructor(File arff) throws Exception {    
    this(arff, 0);
  }  
  
  public QuickIncompleteReconstructor(File arff, int trains) throws Exception {    
    this.arff = arff;
    this.trains = trains;
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
    Logger.info("QuickIncompleteReconstructor: %d elements, %d rankings, %.3f missing rate", sample.getElements().size(), sample.size(), IncompleteUtils.getMissingRate(sample));
    if (trains > 0) {
      QuickIncompleteGenerator generator = new QuickIncompleteGenerator(arff);
      generator.generateParallel(sample, trains);
      classifier = null;
    }
    
    
    if (classifier == null) load();
    
    Instance instance = new DenseInstance(ATTRIBUTES.size()); 
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_ELEMENTS), sample.getElements().size());
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sample.size());
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_MISSING), IncompleteUtils.getMissingRate(sample));
      
    if (center == null) center = CenterReconstructor.reconstruct(sample);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();

    
    // Completer
    double[] bootstraps = new double[QuickIncompleteAttributes.BOOTSTRAPS];
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
