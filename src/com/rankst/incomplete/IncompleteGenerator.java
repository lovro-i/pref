package com.rankst.incomplete;

import com.rankst.filter.Filter;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import static com.rankst.incomplete.IncompleteAttributes.ATTRIBUTES;
import com.rankst.ml.TrainUtils;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.PolynomialReconstructor;
import com.rankst.sample.SampleCompleter;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import com.rankst.triangle.SampleTriangleByRow;
import com.rankst.util.FileUtils;
import com.rankst.util.Logger;
import com.rankst.util.MathUtils;
import com.rankst.util.SystemOut;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


public class IncompleteGenerator {

  
  private File arff;
  private Instances data;
  
  double[] phis = TrainUtils.step(0, 0.95, 0.05);
  
//  private int[] sampleSizes = { 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000 };
//  private double misses[] = TrainUtils.step(0, 0.75, 0.1);
//  private double phis[] = TrainUtils.step(0.05, 0.8, 0.05);
//  private int elems[] = { 5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 180, 200 };
//  private int reps = 10;

  
  
  public IncompleteGenerator(File arff) throws Exception {
    this.arff = arff;
    if (arff.exists()) {
      Logger.info("Loading existing dataset");
      InputStream is = new FileInputStream(arff);
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
      data = source.getDataSet();
      Logger.info("Loaded %d instances", data.size());
    }
    else {
      data = new Instances("Train Incomplete", IncompleteAttributes.ATTRIBUTES, 100);
    }
  }

  
  
  public synchronized void write() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(arff));
    writer.write(data.toString());
    writer.close();
  }
  
  

  
  
  public Instance generateInstance(ElementSet elements, int sampleSize, int resampleSize, double phi, double missing) {
    Instance instance = new DenseInstance(ATTRIBUTES.size());
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_ELEMENTS), elements.size());
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_REAL_PHI), phi);
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sampleSize);
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_RESAMPLE_SIZE), resampleSize);
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_MISSING), missing);


    // Sample
    Ranking center = elements.getReferenceRanking();
    MallowsModel model = new MallowsModel(center, phi);
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    Filter.remove(sample, missing);
    
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    
    // triangle no row
    {
      long start = System.currentTimeMillis();
      SampleTriangle st = new SampleTriangle(center, sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      Logger.info("SampleTriangle reconstructed phi %.3f as %.3f in %.1f sec", phi, mallows.getPhi(), 0.001d * (System.currentTimeMillis() - start));
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_NO_ROW), mallows.getPhi());
    }


    // triangle by row
    {
      long start = System.currentTimeMillis();
      SampleTriangleByRow st = new SampleTriangleByRow(model.getCenter(), sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      Logger.info("SampleTriangleByRow reconstructed phi %.3f as %.3f in %.1f sec", phi, mallows.getPhi(), 0.001d * (System.currentTimeMillis() - start));
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_BY_ROW), mallows.getPhi());
    }

    // Completer
    long start = System.currentTimeMillis();
    double[] bootstraps = new double[IncompleteAttributes.BOOTSTRAPS];
    for (int j = 0; j < bootstraps.length; j++) {
      SampleCompleter completer = new SampleCompleter(sample);
      Sample resample = completer.complete(1);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      bootstraps[j] = mallows.getPhi();
    }
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));


    return instance;
  }
  
  public synchronized void add(Instance instance) {
    instance.setDataset(data);
    data.add(instance);
  }
  
  /** Generates training examples with parameters from the sample: sampleSize, number of elements, missing rate
   * Iterates through <i>phi</i>s, and repeats it <i>reps</i> times for every <i>phi</i>
   * 
   * @param sample Sample to generate similar ones
   * @param reps Number of instances to generate per every phi
   */
  public void generate(Sample sample, int reps) throws Exception {
    long start = System.currentTimeMillis();
    IncompleteGenerator generator = new IncompleteGenerator(arff);
    int count = 0;
    double missing = IncompleteUtils.getMissingRate(sample);    
    for (int i = 0; i < reps; i++) {
      for (double phi: phis) {
        SystemOut.println("Training pass %d, phi %.2f", i, phi);
        Instance instance = generator.generateInstance(sample.getElements(), sample.size(), IncompleteAttributes.RESAMPLE_SIZE, phi, missing);
        generator.add(instance);
        count++;
      }
      generator.write();
    }
    SystemOut.println("%d instances generated in %d sec", count, (System.currentTimeMillis() - start) / 1000);
  }
  
  
  
  
  /** Parallel implementation of method generate, with <code>reps</code> number of threads
   * 
   * @param sample Sample to generate similar ones (size, number of elements, missing rate)
   * @param reps Number of threads per phi
   */
  public void generateParallel(Sample sample, int reps) throws Exception {
    long start = System.currentTimeMillis();
    IncompleteGenerator generator = new IncompleteGenerator(arff);
    
    //SystemOut.mute();
    List<Trainer> trainers = new ArrayList<Trainer>();
    for (int id = 1; id <= reps; id++) {
      Trainer trainer = new Trainer(sample);
      trainer.start();
      trainers.add(trainer);
    }
    
    for (Trainer trainer: trainers) trainer.join();
    
    generator.write();
    //SystemOut.unmute();
    Logger.info("%d instance series generated in %d sec", reps, (System.currentTimeMillis() - start) / 1000);
  }

  private int nextId = 1;
  
  private class Trainer extends Thread {

    private final Sample sample;
    private final int id;

    private Trainer(Sample sample) {
      this.id = nextId++;
      this.sample = sample;
    }
    
    @Override
    public void run() {
      long start = System.currentTimeMillis();
      double missing = IncompleteUtils.getMissingRate(sample);
      for (double phi: phis) {
        Logger.info("Trainer #%d, phi %.2f", id, phi);
        Instance instance = generateInstance(sample.getElements(), sample.size(), IncompleteAttributes.RESAMPLE_SIZE, phi, missing);
        add(instance);
      }
      Logger.info("Trainer #%d finished in %d sec", id, (System.currentTimeMillis() - start) / 1000);
    }
  }
  
  
  public static void main(String[] args) throws Exception {
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    IncompleteGenerator generator = new IncompleteGenerator(new File(folder, "incomplete.train.arff"));
  }
}
