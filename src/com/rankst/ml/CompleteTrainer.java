package com.rankst.ml;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.generator.Resampler;
import static com.rankst.ml.CompleteAttributes.ATTRIBUTES;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.DirectReconstructor;
import com.rankst.reconstruct.DirectReconstructorSmart;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.util.MathUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/** Generates requested train data and writes it into arff file */
public class CompleteTrainer {

  private File arff;
  private Instances data;
  
  public CompleteTrainer(File arff) throws Exception {
    this.arff = arff;
    if (arff.exists()) {
      System.out.println("Loading existing dataset");
      InputStream is = new FileInputStream(arff);
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
      data = source.getDataSet();
      System.out.println("Loaded instances: " + data.size());
    }
  }
  
  
    
  private synchronized void add(Instance instance) {
    instance.setDataset(data);
    data.add(instance);
  }
  
  public synchronized void write() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(arff));
    writer.write(data.toString());
    writer.close();
  }
  
  public void generateTrainData(int n, int[] samples, double phis[], int reps, int bootstraps) {
    ElementSet elements = new ElementSet(n);
    Ranking reference = elements.getReferenceRanking();
    
    for (int sampleSize: samples) {
      for (double phi: phis) {
        System.out.println(String.format("n = %d; samples = %d, phi = %2f", n, sampleSize, phi));
        for (int i = 0; i < reps; i++) {
          generateTrainData(reference, sampleSize, phi, bootstraps);
        }
      }
    }
  }
  
  public void generateTrainData(Ranking reference, int sampleSize, double phi, int bootstraps) {
    
    Instance instance = new DenseInstance(ATTRIBUTES.size());
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_ELEMENTS), reference.size());
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_REAL_PHI), phi);
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sampleSize);
    

    // Sample
    MallowsTriangle triangle = new MallowsTriangle(reference, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);    

    // No Bootstrap
    MallowsModel direct = new DirectReconstructorSmart().reconstruct(sample);
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_DIRECT_PHI), direct.getPhi());

    // Bootstrap
    Resampler resampler = new Resampler(sample);          
    double boots[] = new double[bootstraps];
    for (int j = 0; j < bootstraps; j++) {
      Sample resample = resampler.resample();
      MallowsModel m = new DirectReconstructorSmart().reconstruct(resample);
      boots[j] = m.getPhi();
    }
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_BOOTSTRAP_PHI), MathUtils.mean(boots));
    instance.setValue(ATTRIBUTES.indexOf(CompleteAttributes.ATTRIBUTE_BOOTSTRAP_VAR), MathUtils.variance(boots));
    
    this.add(instance);
  }
  
  
}
