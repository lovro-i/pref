package com.rankst.incomplete;

import com.rankst.comb.Comb;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.generator.Resampler;
import static com.rankst.incomplete.IncompleteAttributes.ATTRIBUTES;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.DirectReconstructor;
import com.rankst.reconstruct.MallowsReconstructor;
import com.rankst.sample.SampleCompleter;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import com.rankst.triangle.SampleTriangleByRow;
import com.rankst.util.MathUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.M5P;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class IncompleteReconstructor implements MallowsReconstructor {

  private M5P classifier;
  
  public IncompleteReconstructor(File train) throws Exception {
    long start = System.currentTimeMillis();
    InputStream is = new FileInputStream(train);
    ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
    Instances data = source.getDataSet();
    data.setClassIndex(data.numAttributes() - 1);
    
    classifier = new M5P();
    classifier.setMinNumInstances(4);    
    classifier.buildClassifier(data);
    System.out.println(String.format("Incomplete regression classifier learnt in %d ms", System.currentTimeMillis() - start));
  }  

  
  @Override
  public MallowsModel reconstruct(Sample sample) throws Exception {
    int resampleSize = IncompleteAttributes.RESAMPLE_SIZE;
    
    Instance instance = new DenseInstance(ATTRIBUTES.size()); 
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_ELEMENTS), sample.getElements().size());
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sample.size());
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_RESAMPLE_SIZE), resampleSize);
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_MISSING), IncompleteUtils.getMissingRate(sample));
      
    Ranking center;
    
    // triangle no row
    {
      SampleTriangle st = new SampleTriangle(sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      DirectReconstructor rec = new DirectReconstructor();
      MallowsModel mallows = rec.reconstruct(resample);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_NO_ROW), mallows.getPhi());
      center = mallows.getCenter();
    }


    // triangle by row
    {
      SampleTriangleByRow st = new SampleTriangleByRow(sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      DirectReconstructor rec = new DirectReconstructor();
      MallowsModel mallows = rec.reconstruct(resample);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_BY_ROW), mallows.getPhi());
    }

    
    // Completer
    double[] bootstraps = new double[IncompleteAttributes.BOOTSTRAPS];
    for (int j = 0; j < bootstraps.length; j++) {
      SampleCompleter completer = new SampleCompleter(sample);
      Sample resample = completer.complete(1);
      DirectReconstructor rec = new DirectReconstructor();
      MallowsModel mallows = rec.reconstruct(resample);
      bootstraps[j] = mallows.getPhi();
    }
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));

    
    double regressionPhi = this.classifier.classifyInstance(instance);
    return new MallowsModel(center, regressionPhi);
  }
  
  public static void main(String[] args) throws Exception {
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    File file = new File(folder, "incomplete.train.arff");
    IncompleteReconstructor reconstructor = new IncompleteReconstructor(file);
    
    int n = 10;
    ElementSet elements = new ElementSet(n);
    
    double phi = 0.35;
    double missing = 0.25;
    int sampleSize = 5000;
    
    MallowsTriangle triangle = new MallowsTriangle(elements.getReferenceRanking(), phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    Comb.comb(sample, missing);
    
    MallowsModel model = reconstructor.reconstruct(sample);
    System.out.println(model);
  }
}
