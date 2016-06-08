package edu.drexel.cs.db.db4pref.reconstruct.noisy;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.sampler.RIMSampler;
import edu.drexel.cs.db.db4pref.sampler.Resampler;
import edu.drexel.cs.db.db4pref.util.TrainUtils;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.db4pref.sampler.triangle.MallowsTriangle;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.util.ArrayList;
import java.util.List;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import weka.core.Attribute;


public class NoisyGenerator {

  private final boolean triangle;
  private final int boots;
  
  private double[] phis = TrainUtils.step(0, 0.55, 0.05);
  private double[] noises = TrainUtils.step(0.0, 0.3, 0.05);
  
  private final ArrayList<Attribute> attributes;
  private final Instances data;
  
  
  
  public NoisyGenerator(boolean triangle, int bootstraps) throws Exception {
    this.triangle = triangle;
    this.boots = bootstraps;
    this.attributes = NoisyAttributes.getAttributes(triangle, boots);
    this.data = new Instances("Train Noisy", attributes, 100);
  }
  
  public Instances getInstances() {
    return data;
  }
  
  public void setTrainPhiRange(double minPhi, double maxPhi, double phiStep) {
    this.phis = TrainUtils.step(minPhi, maxPhi, phiStep);
  }
  
  public void setTrainNoiseRange(double minNoise, double maxNoise, double stepNoise) {
    this.phis = TrainUtils.step(minNoise, maxNoise, stepNoise);
  }
  
  public ArrayList<Attribute> getAttributes() {
    return attributes;
  }
    
  public synchronized void add(Instance instance) {
    instance.setDataset(data);
    data.add(instance);
  }
  
  public Instance generateInstance(ItemSet items, int sampleSize, double phi, double noise) {    
    Instance instance = new DenseInstance(attributes.size());
    instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_ITEMS), items.size());
    instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_REAL_PHI), phi);
    instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_SAMPLE_SIZE), sampleSize);


    // Sample
    Ranking center = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(center, phi);
    MallowsTriangle mTriangle = new MallowsTriangle(model);
    RIMSampler sampler = new RIMSampler(mTriangle);
    RankingSample sample = sampler.sample(sampleSize);
    Filter.noise(sample, noise);
    
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
        Sample<? extends PreferenceSet> resample = resampler.resample();
        MallowsModel m = reconstructor.reconstruct(resample, center);
        bootstraps[j] = m.getPhi();
      }
      instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_BOOTSTRAP_MEAN), MathUtils.mean(bootstraps));
      instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_BOOTSTRAP_MIN), MathUtils.min(bootstraps));
      instance.setValue(attributes.indexOf(NoisyAttributes.ATTRIBUTE_BOOTSTRAP_VAR), MathUtils.variance(bootstraps));
    }

    return instance;
  }
  
  
  
  
  
  /** Generates training examples with parameters from the sample: sampleSize, number of items
   * Iterates through <i>phi</i>s and <i>noise</i>s, and repeats it <i>reps</i> times for every <i>phi</i>
   * 
   * @param sample Sample to generate similar ones
   * @param reps Number of instances to generate per every phi
   */
  public Instances generate(RankingSample sample, int reps) throws Exception {
    long start = System.currentTimeMillis();
    int count = 0;
    
    for (int i = 0; i < reps; i++) {
      for (double phi: phis) {
        for (double noise: noises) {
          Logger.info("Training pass %d, phi %.2f, noise %.0f%%", i, phi, 100 * noise);
          Instance instance = generateInstance(sample.getItemSet(), sample.size(), phi, noise);
          add(instance);
          count++;
        }
      }
    }
    Logger.info("%d instances generated in %d sec", count, (System.currentTimeMillis() - start) / 1000);
    return data;
  }
  
  
  private ItemSet items;
  private int sampleSize;
  private Queue<Double> queue = new LinkedBlockingQueue<Double>();
  
  /** Parallel implementation of method train, with <code>reps</code> number of threads
   * 
   * @param sample Sample to generate similar ones (size, number of items, missing rate)
   * @param reps Number of threads per phi
   */
  public Instances generate(Sample<Ranking> sample, int reps, int threads) throws Exception {
    long start = System.currentTimeMillis();
    
    this.items = sample.getItemSet();
    this.sampleSize = sample.size();
    
    for (int r = 0; r < reps; r++) {
      for (int i = 0; i < phis.length; i++) {
        queue.add(phis[i]);
      }
    }
    
    List<Trainer> trainers = new ArrayList<Trainer>();    
    for (int id = 1; id <= threads; id++) {
      Trainer trainer = new Trainer();
      trainer.start();
      trainers.add(trainer);
    }
    
    for (Trainer trainer: trainers) trainer.join();
    Logger.info("%d instance series generated in %d sec", reps, (System.currentTimeMillis() - start) / 1000);
    return data;
  }
 
  private int nextId = 1;
  
  private class Trainer extends Thread {

    private final int id;

    private Trainer() {
      this.id = nextId++;
    }
    
    @Override
    public void run() {
      long start = System.currentTimeMillis();
      while (true) {
        Double phi = queue.poll();
        if (phi == null) break;

        // Logger.info("Trainer #%d, phi %.2f", id, phi);
        for (double noise: noises) {
          Instance instance = generateInstance(items, sampleSize, phi, noise);
          add(instance);
        }
      }
      // Logger.info("Trainer #%d finished in %d sec", id, (System.currentTimeMillis() - start) / 1000);
    }

  }
  
}
