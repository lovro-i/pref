package edu.drexel.cs.db.rank.noisy;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.generator.Resampler;
import edu.drexel.cs.db.rank.util.TrainUtils;
import edu.drexel.cs.db.rank.model.MallowsModel;
import static edu.drexel.cs.db.rank.noisy.NoisyAttributes.ATTRIBUTES;
import static edu.drexel.cs.db.rank.noisy.NoisyAttributes.ATTRIBUTE_REAL_PHI;
import static edu.drexel.cs.db.rank.noisy.NoisyAttributes.ATTRIBUTE_SAMPLE_SIZE;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import static edu.drexel.cs.db.rank.noisy.NoisyAttributes.ATTRIBUTE_ITEMS;


public class NoisyGenerator {

  private double[] phis = TrainUtils.step(0, 0.55, 0.05);
  private double[] noises = TrainUtils.step(0.0, 0.3, 0.05);
  
  private File arff;
  private Instances data;
  
  
  public NoisyGenerator(File arff) throws Exception {
    this.arff = arff;
    if (arff.exists()) {
      Logger.info("Loading existing dataset");
      InputStream is = new FileInputStream(arff);
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
      data = source.getDataSet();
      Logger.info("Loaded %d instances", data.size());
    }
    else {
      System.out.println("Creating new dataset instances");
      data = new Instances("Train Incomplete", NoisyAttributes.ATTRIBUTES, 100);
    }
  }
  
  public synchronized void write() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(arff));
    writer.write(data.toString());
    writer.close();
  }
  
  public synchronized void add(Instance instance) {
    instance.setDataset(data);
    data.add(instance);
  }
  
  public Instance generateInstance(ItemSet items, int sampleSize, double phi, double noise) {
    Instance instance = new DenseInstance(ATTRIBUTES.size());
    instance.setValue(ATTRIBUTES.indexOf(ATTRIBUTE_ITEMS), items.size());
    instance.setValue(ATTRIBUTES.indexOf(ATTRIBUTE_REAL_PHI), phi);
    instance.setValue(ATTRIBUTES.indexOf(ATTRIBUTE_SAMPLE_SIZE), sampleSize);


    // Sample
    Ranking center = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(center, phi);
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    Filter.noise(sample, noise);
    
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    
    // triangle no row
    {
      MallowsModel mallows = reconstructor.reconstruct(sample, center);
      instance.setValue(ATTRIBUTES.indexOf(NoisyAttributes.ATTRIBUTE_DIRECT_PHI), mallows.getPhi());
    }

    
    // Bootstrap
    {
      Resampler resampler = new Resampler(sample);
      double bootstraps[] = new double[NoisyAttributes.BOOTSTRAPS];
      for (int j = 0; j < bootstraps.length; j++) {
        Sample resample = resampler.resample();
        MallowsModel m = reconstructor.reconstruct(resample, center);
        bootstraps[j] = m.getPhi();
      }
      instance.setValue(ATTRIBUTES.indexOf(NoisyAttributes.ATTRIBUTE_BOOTSTRAP_PHI), MathUtils.mean(bootstraps));
      instance.setValue(ATTRIBUTES.indexOf(NoisyAttributes.ATTRIBUTE_BOOTSTRAP_VAR), MathUtils.variance(bootstraps));
    }

    return instance;
  }
  
  
  
  
  
  /** Generates training examples with parameters from the sample: sampleSize, number of items
   * Iterates through <i>phi</i>s and <i>noise</i>s, and repeats it <i>reps</i> times for every <i>phi</i>
   * 
   * @param sample Sample to generate similar ones
   * @param reps Number of instances to generate per every phi
   */
  public void generate(Sample sample, int reps) throws Exception {
    long start = System.currentTimeMillis();
    NoisyGenerator generator = new NoisyGenerator(arff);
    int count = 0;
    
    for (int i = 0; i < reps; i++) {
      for (double phi: phis) {
        for (double noise: noises) {
          Logger.info("Training pass %d, phi %.2f, noise %.0f%%", i, phi, 100 * noise);
          Instance instance = generator.generateInstance(sample.getItemSet(), sample.size(), phi, noise);
          generator.add(instance);
          count++;
        }
      }
      generator.write();
    }
    Logger.info("%d instances generated in %d sec", count, (System.currentTimeMillis() - start) / 1000);
  }
  
  
  /** Parallel implementation of method train, with <code>reps</code> number of threads
   * 
   * @param sample Sample to generate similar ones (size, number of items, missing rate)
   * @param reps Number of threads per phi
   */
  public void generateParallel(Sample sample, int reps) throws Exception {
    long start = System.currentTimeMillis();
    NoisyGenerator generator = new NoisyGenerator(arff);
    
    List<Trainer> trainers = new ArrayList<Trainer>();    
    for (int id = 1; id <= reps; id++) {
      for (double noise: noises) {
        Trainer trainer = new Trainer(sample, noise);
        trainer.start();
        trainers.add(trainer);
      }
    }
    
    for (Trainer trainer: trainers) trainer.join();
    
    generator.write();
    Logger.info("%d instance series generated in %d sec", reps, (System.currentTimeMillis() - start) / 1000);
  }
 
  private int nextId = 1;
  
  private class Trainer extends Thread {

    private final Sample sample;
    private final int id;
    private double noise;

    private Trainer(Sample sample, double noise) {
      this.id = nextId++;
      this.sample = sample;
      this.noise = noise;
    }
    
    @Override
    public void run() {
      long start = System.currentTimeMillis();
      double[] phis = TrainUtils.step(0, 0.5, 0.05);      
      for (double phi: phis) {
        Logger.info("Trainer #%d, phi %.2f, noise %.2f", id, phi, noise);
        Instance instance = generateInstance(sample.getItemSet(), sample.size(), phi, noise);
        add(instance);
      }
      Logger.info("Trainer #%d finished in %d sec", id, (System.currentTimeMillis() - start) / 1000);
    }
  }
  
}
