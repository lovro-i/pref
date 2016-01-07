package edu.drexel.cs.db.rank.ml;

import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.generator.Resampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.histogram.Histogram;
import static edu.drexel.cs.db.rank.ml.CompleteAttributes.ATTRIBUTES;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.util.MathUtils;
import edu.drexel.cs.db.rank.util.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/** Reconstructs Mallows Model from the sample using ensemble of direct and bootstrap reconstruction (regression) */
@Deprecated
public class RegressionReconstructor implements MallowsReconstructor {

  private AbstractClassifier model;
  private int bootstraps = 10;
  
  private CompleteReconstructor directReconstructor;
  
  
  /** Create reconstructor with the arff train data file from which to learn regressor */
  public RegressionReconstructor(File train, CompleteReconstructor directReconstructor) throws Exception {
    this.directReconstructor = directReconstructor;
    InputStream is = new FileInputStream(train);
    ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
    Instances data = source.getDataSet();
    data.setClassIndex(data.numAttributes() - 1);
    
    M5P model = new M5P();
    model.setMinNumInstances(4);    
    model.buildClassifier(data); 
    this.model = model;
  }
  
  
  @Override
  public MallowsModel reconstruct(Sample sample) throws Exception {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }
  
  
  
  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) throws Exception {
    
    // No Bootstrap
    MallowsModel direct = directReconstructor.reconstruct(sample, center);        

    // Bootstrap
    Resampler resampler = new Resampler(sample);          
    double boots[] = new double[bootstraps];
    for (int j = 0; j < bootstraps; j++) {
      Sample resample = resampler.resample();
      MallowsModel m = directReconstructor.reconstruct(resample, center);
      boots[j] = m.getPhi();
    }

          
    // Regression
    Instance instance = new DenseInstance(CompleteAttributes.ATTRIBUTES.size());
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_ELEMENTS), sample.getElements().size());
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sample.size());
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_DIRECT_PHI), direct.getPhi());
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_BOOTSTRAP_PHI), MathUtils.mean(boots));
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_BOOTSTRAP_VAR), MathUtils.variance(boots));
        
    double regressionPhi = this.model.classifyInstance(instance);
    return new MallowsModel(center, regressionPhi);
  }
  
}
