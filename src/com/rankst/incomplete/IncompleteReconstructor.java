package com.rankst.incomplete;

import com.rankst.comb.Comb;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import static com.rankst.incomplete.IncompleteAttributes.ATTRIBUTES;
import com.rankst.ml.TrainUtils;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.CenterReconstructor;
import com.rankst.reconstruct.MallowsReconstructor;
import com.rankst.reconstruct.PolynomialReconstructor;
import com.rankst.sample.SampleCompleter;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import com.rankst.triangle.SampleTriangleByRow;
import com.rankst.util.Logger;
import com.rankst.util.MathUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
  
  public IncompleteReconstructor(File arff) throws Exception {    
    this.arff = arff;
    load();
  }  
  
  private void load() throws Exception {
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

  /** Generates training examples with parameters from the sample: sampleSize, number of elements, missing rate
   * Iterates through <i>phi</i>s, and repeats it <i>reps</i> times for every <i>phi</i>
   * 
   * @param sample Sample to generate similar ones
   * @param reps Number of instances to generate per every phi
   */
  public void train(Sample sample, int reps) throws Exception {
    long start = System.currentTimeMillis();
    IncompleteGenerator generator = new IncompleteGenerator(arff);
    int count = 0;
    double missing = IncompleteUtils.getMissingRate(sample);
    double[] phis = TrainUtils.step(0.05, 0.95, 0.05);
    for (int i = 0; i < reps; i++) {
      for (double phi: phis) {
        Logger.info("Training pass %d, phi %.2f", i, phi);
        Instance instance = generator.generateInstance(sample.getElements(), sample.size(), IncompleteAttributes.RESAMPLE_SIZE, phi, missing);
        generator.add(instance);
        count++;
      }
      generator.write();
    }
    Logger.info("%d instances generated in %d sec", count, (System.currentTimeMillis() - start) / 1000);
    load();
  }
  
  
  /** Parallel implementation of method train, with <code>reps</code> number of threads
   * 
   * @param sample Sample to generate similar ones (size, number of elements, missing rate)
   * @param reps Number of threads per phi
   */
  public void trainParallel(Sample sample, int reps) throws Exception {
    long start = System.currentTimeMillis();
    IncompleteGenerator generator = new IncompleteGenerator(arff);
    
    List<Trainer> trainers = new ArrayList<Trainer>();
    for (int id = 1; id <= reps; id++) {
      Trainer trainer = new Trainer(id, sample, generator);
      trainer.start();
      trainers.add(trainer);
    }
    
    for (Trainer trainer: trainers) trainer.join();
    
    generator.write();
    Logger.info("%d instance series generated in %d sec", reps, (System.currentTimeMillis() - start) / 1000);
    load();
  }
  
  private class Trainer extends Thread {

    private final IncompleteGenerator generator;
    private final Sample sample;
    private final int id;

    private Trainer(int id, Sample sample, IncompleteGenerator generator) {
      this.id = id;
      this.sample = sample;
      this.generator = generator;
    }
    
    @Override
    public void run() {
      long start = System.currentTimeMillis();
      double missing = IncompleteUtils.getMissingRate(sample);
      double[] phis = TrainUtils.step(0, 0.95, 0.05);
      for (double phi: phis) {
        Logger.info("Trainer #%d, phi %.2f", id, phi);
        Instance instance = generator.generateInstance(sample.getElements(), sample.size(), IncompleteAttributes.RESAMPLE_SIZE, phi, missing);
        generator.add(instance);
      }
      Logger.info("Trainer #%d finished in %d sec", id, (System.currentTimeMillis() - start) / 1000);
    }
  }
  
  @Override
  public MallowsModel reconstruct(Sample sample) throws Exception {
    int resampleSize = IncompleteAttributes.RESAMPLE_SIZE;
    
    Instance instance = new DenseInstance(ATTRIBUTES.size()); 
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_ELEMENTS), sample.getElements().size());
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sample.size());
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_RESAMPLE_SIZE), resampleSize);
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_MISSING), IncompleteUtils.getMissingRate(sample));
      
    Ranking center = CenterReconstructor.reconstruct(sample);
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
    double phi = 0.25;
    double missing = 0.55;
    int sampleSize = 3000;
    
    MallowsModel original = new MallowsModel(elements.getRandomRanking(), phi);
    
    MallowsTriangle triangle = new MallowsTriangle(original);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    Comb.comb(sample, missing);
    
    //reconstructor.trainParallel(sample, 6);
    MallowsModel model = reconstructor.reconstruct(sample);
    System.out.println();
    System.out.println("     Original Mallows Model: " + original);
    System.out.println("Reconstructed Mallows Model: " + model);
  }
}
