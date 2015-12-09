package com.rankst.ml;

import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.Resampler;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.DirectReconstructor;
import com.rankst.reconstruct.MallowsReconstructor;
import com.rankst.histogram.Histogram;
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

  public static final Attribute ATTRIBUTE_SAMPLES = new Attribute("samples");
  public static final Attribute ATTRIBUTE_DIRECT_PHI = new Attribute("direct_phi");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_PHI = new Attribute("bootstrap_phi");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_VAR = new Attribute("bootstrap_var");
  public static final Attribute ATTRIBUTE_REAL_PHI = new Attribute("real_phi");
  
  public static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();
  
  private AbstractClassifier model;
  private int bootstraps = 10;
  
  static {
    ATTRIBUTES.add(ATTRIBUTE_SAMPLES);
    ATTRIBUTES.add(ATTRIBUTE_DIRECT_PHI);
    ATTRIBUTES.add(ATTRIBUTE_BOOTSTRAP_PHI);
    ATTRIBUTES.add(ATTRIBUTE_BOOTSTRAP_VAR);
    ATTRIBUTES.add(ATTRIBUTE_REAL_PHI);
  }
  
  /** Create reconstructor with the arff train data file from which to learn regressor */
  public RegressionReconstructor(File train) throws Exception {
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
    MallowsModel direct = new DirectReconstructor().reconstruct(sample);        

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
    instance.setValue(0, sample.size());
    instance.setValue(1, direct.getPhi());
    instance.setValue(2, phib);
    instance.setValue(3, Utils.variance(boots));
        
    double regressionPhi = this.model.classifyInstance(instance);
    return new MallowsModel(direct.getCenter(), regressionPhi);
  }
  
}
