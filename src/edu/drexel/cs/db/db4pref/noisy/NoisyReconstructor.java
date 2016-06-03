package edu.drexel.cs.db.db4pref.noisy;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.sampler.Resampler;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.db4pref.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.util.ArrayList;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/** In case there is noise in the sample, direct and bootstrap reconstructor should differ. We can use this to reconstruct the correct model */
public class NoisyReconstructor implements MallowsReconstructor {

  private final boolean triangle;
  private final int boots;
  private int trainSeries = 3;
  private int threads = Runtime.getRuntime().availableProcessors();
  
  
  public NoisyReconstructor(boolean triangle, int bootstraps) throws Exception {    
    this.triangle = triangle;
    this.boots = bootstraps;
  }  
  

  public void setTrainSeries(int series) {
    this.trainSeries = series;
  }
  
  @Override
  public MallowsModel reconstruct(Sample sample) throws Exception {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }

  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) throws Exception {
    NoisyGenerator generator = new NoisyGenerator(triangle, boots);
    Instances data = generator.generate(sample, trainSeries, threads);
    data.setClassIndex(data.numAttributes() - 1);    
    M5P classifier = new M5P();
    classifier.setMinNumInstances(4);    
    classifier.buildClassifier(data);
    
    ArrayList<Attribute> attributes = generator.getAttributes();
    
    Instance instance = new DenseInstance(attributes.size()); 
    instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_ITEMS), sample.getItemSet().size());
    instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_SAMPLE_SIZE), sample.size());
      
    if (center == null) center = CenterReconstructor.reconstruct(sample);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();


    // triangle no row
    if (triangle) {
      MallowsModel mallows = reconstructor.reconstruct(sample, center);
      instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_DIRECT_PHI), mallows.getPhi());
    }

    
    // Bootstrap
    if (boots > 0) {
      Resampler resampler = new Resampler(sample);
      double bootstraps[] = new double[boots];
      for (int j = 0; j < bootstraps.length; j++) {
        Sample resample = resampler.resample();
        MallowsModel m = reconstructor.reconstruct(resample, center);
        bootstraps[j] = m.getPhi();
      }
      instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_BOOTSTRAP_MEAN), MathUtils.mean(bootstraps));
      instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_BOOTSTRAP_MIN), MathUtils.min(bootstraps));
      instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_BOOTSTRAP_VAR), MathUtils.variance(bootstraps));
    }

    Logger.info("Instance to be classified: %s", instance);
    double regressionPhi = classifier.classifyInstance(instance);
    return new MallowsModel(center, regressionPhi);
  }

  
  
  
  
  
  public static void main(String[] args) {
    System.out.println("NoisyReconstructor");
  }
}
