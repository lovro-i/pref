package com.rankst.ml;

import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.Resampler;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.DirectReconstructor;
import com.rankst.reconstruct.MallowsReconstructor;
import com.rankst.histogram.Histogram;
import static com.rankst.ml.CompleteAttributes.ATTRIBUTES;
import com.rankst.reconstruct.DirectReconstructorSmart;
import com.rankst.util.MathUtils;
import com.rankst.util.Utils;
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
public class RegressionReconstructor implements MallowsReconstructor {

  private AbstractClassifier model;
  private int bootstraps = 10;
  
  private DirectReconstructor directReconstructor;
  
  
  /** Create reconstructor with the arff train data file from which to learn regressor */
  public RegressionReconstructor(File train, DirectReconstructor directReconstructor) throws Exception {
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
    
    // No Bootstrap
    MallowsModel direct = directReconstructor.reconstruct(sample);        

    // Bootstrap
    Resampler resampler = new Resampler(sample);          
    double boots[] = new double[bootstraps];
    for (int j = 0; j < bootstraps; j++) {
      Sample resample = resampler.resample();
      MallowsModel m = directReconstructor.reconstruct(resample);
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
    return new MallowsModel(direct.getCenter(), regressionPhi);
  }
  
}
