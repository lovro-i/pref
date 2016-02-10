package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.filter.SampleCompleter;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.triangle.Expands;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangleByRowNew;
import edu.drexel.cs.db.rank.util.Config;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import edu.drexel.cs.db.rank.util.TrainUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


public class IgnorantIncompleteGenerator {

  private final ArrayList<Attribute> attributes;  
  private Instances data;
  
  private double[] phis = TrainUtils.step(0, 1, 0.1);
  
  public IgnorantIncompleteGenerator() throws Exception {
    this.attributes = IgnorantIncompleteAttributes.getAttributes();
    this.data = new Instances("Train Incomplete", attributes, 1);
  }

  public Instance generateInstance(int items, int sampleSize, int resampleSize, double phi, double missing, int boots) throws InterruptedException {
    return generateInstance(new ItemSet(items), sampleSize, resampleSize, phi, missing, boots);
  }
  
  public Instance generateInstance(ItemSet items, int sampleSize, int resampleSize, double phi, double missing, int boots) throws InterruptedException {
    Instance instance = new DenseInstance(attributes.size());
    instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_ITEMS), items.size());
    instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_MISSING_RATE), missing);
    instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sampleSize);
    instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_RESAMPLE_SIZE), resampleSize);
    instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_BOOTSTRAPS), boots);
    instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_TRIANGLE_THRESHOLD), Expands.getThreshold());
    instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_REAL_PHI), phi);
    
    
    // Sample
    Ranking center = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(center, phi);
    MallowsTriangle mallowsTriangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(mallowsTriangle);
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
      instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_TRIANGLE_NO_ROW), mallows.getPhi());
    }


    // triangle by row
    {
      long start = System.currentTimeMillis();
      SampleTriangleByRowNew st = new SampleTriangleByRowNew(model.getCenter(), sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      Logger.info("SampleTriangleByRow reconstructed phi %.3f as %.3f in %.1f sec", phi, mallows.getPhi(), 0.001d * (System.currentTimeMillis() - start));
      instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_TRIANGLE_BY_ROW), mallows.getPhi());
    }

    // Completer
    {
      double[] bootstraps = new double[boots];
      for (int j = 0; j < bootstraps.length; j++) {
        SampleCompleter completer = new SampleCompleter(sample);
        Sample resample = completer.complete(1);
        MallowsModel mallows = reconstructor.reconstruct(resample, center);
        bootstraps[j] = mallows.getPhi();
      }
      instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
      instance.setValue(attributes.indexOf(IgnorantIncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));
    }

    return instance;
  }
  
  public synchronized void add(Instance instance) {
    instance.setDataset(data);
    data.add(instance);
  }
    
  public synchronized void load(File arff) throws Exception {
    if (arff.exists() && arff.length() > 0) {
      Logger.info("Loading existing dataset");
      InputStream is = new FileInputStream(arff);
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
      data = source.getDataSet();
      Logger.info("Loaded %d instances", data.size());
    }
  }
  
  public synchronized void save(File arff) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(arff));
    writer.write(data.toString());
    writer.close();
  }
    
  public Instances generate() throws Exception {
    long start = System.currentTimeMillis();
    
    int boots = 50;
    int[] samples = { 1000, 2000, 5000, 10000 };
    int[] itemSizes = { 20, 50, 80 };
    double[] phis = { 0.1, 0.3, 0.5, 0.7, 0.9 };
    double[] misses = { 0.1, 0.3, 0.5, 0.7, 0.9 };    
    int resampleSize = 10000;

    for (int sampleSize: samples) {
      for (int itemSize: itemSizes) {        
        ItemSet item = new ItemSet(itemSize);
        for (double phi: phis) {
          Logger.info("Samples %d, items %d, phi %.1f", sampleSize, itemSize, phi);
          for (double miss: misses) {
            Instance instance = this.generateInstance(item, resampleSize, resampleSize, phi, miss, boots);
            this.add(instance);
          }
        }
      }
    }
    
    Logger.info("Training set generated in %d sec", (System.currentTimeMillis() - start) / 1000);
    return data;
  }
  
  
  private Saver saver = null;
  
  public synchronized void startSaver(File arff, int period) {
    if (saver == null) {
      saver = new Saver(arff, period);
      saver.start();
    }
    else {
      saver.setFile(arff);
      saver.setPeriod(period);
    }
  }
  
  public synchronized void stopSaver() {
    if (saver != null) saver.end();
  }
  
  private class Saver extends Thread {
    
    boolean done = false;
    private int period;
    private File arff;
    
    private Saver(File arff, int period) {
      this.arff = arff;
      this.period = period;
    } 
    
    private void setFile(File arff) {
      this.arff = arff;
    }
    
    private void setPeriod(int period) {
      this.period = period;
    }
    
    public void run() {
      try {
      while (!done) {
        sleep(period);
        if (done) return;
        save(arff);
      }
      }
      catch (Exception e) {}
    }
    
    public void end() {
      this.done = true;
    }
  }
  
  public static void main(String[] args) throws Exception {
    File arff;
    if (args.length > 0) arff = new File(args[0]);
    else arff = new File(Config.RESULTS_FOLDER, "ignorant.train.2.arff");
    
    IgnorantIncompleteGenerator generator = new IgnorantIncompleteGenerator();
    generator.load(arff);
//    generator.startSaver(arff, 15000);
//    generator.generate();
//    generator.stopSaver();
//    generator.save(arff);


    double[] phis = { 0.2, 0.5, 0.8 };
    double[] misses = { 0.2, 0.5, 0.8 };    
    int[] sampleSizes = { 100, 1000, 10000 };
    int[] itemss = { 100, 5, 10, 20, 30, 50 };
    int reps = 1000;
    double[] thresholds = { 0.0001, 0.001, 0.01, 0.1 };
    
    

    for (int i = 0; i < reps; i++) {
      double phi = 0.2; // phis[MathUtils.RANDOM.nextInt(phis.length)];
      double miss = 0.2; // misses[MathUtils.RANDOM.nextInt(misses.length)];
      /// int sampleSize = sampleSizes[MathUtils.RANDOM.nextInt(sampleSizes.length)];
      for (int items: itemss) {
        //for (double threshold: thresholds) {
          double threshold = 0.05;
          Expands.setThreshold(threshold);
          int sampleSize = 1000;
          Instance a = generator.generateInstance(items, sampleSize, 1000, phi, miss, 20);
          System.out.println(a);
          generator.add(a);
          generator.save(arff);        
        //}
      }
      System.out.println("---");
    }    
  }
  
}
