package com.rankst.incomplete;

import com.rankst.comb.Comb;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.generator.Resampler;
import static com.rankst.incomplete.IncompleteAttributes.ATTRIBUTES;
import com.rankst.ml.TrainUtils;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.DirectReconstructor;
import com.rankst.sample.SampleCompleter;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import com.rankst.triangle.SampleTriangleByRow;
import com.rankst.util.MathUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.M5P;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class IncompleteTrainer {
  
  private File file;
  private Instances data;
  
  
  public IncompleteTrainer(File file) throws Exception {
    this.file = file;
    if (file.exists()) {
      System.out.println("Loading existing dataset");
      InputStream is = new FileInputStream(file);
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
      data = source.getDataSet();
      System.out.println("Loaded instances: " + data.size());
    }
  }
  
  private void write() throws IOException {
    synchronized (data) {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(data.toString());
      writer.close();
    }
  }
  
  private int[] sampleSizes = { 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000 }; //, 200000, 500000, 1000000 };
  private int rep = 20;
  double phis[] = TrainUtils.step(0.05, 0.8, 0.05);
  double misses[] = TrainUtils.step(0.2, 0.5, 0.05);
  int n = 10;
  ElementSet elements = new ElementSet(n);
    
  
  private void buildDataset() throws IOException, InterruptedException {    
    int size = phis.length * sampleSizes.length * misses.length * rep;
    System.out.println("Generating " + rep + " training instances per line");
    
    if (data == null) {
      System.out.println("Creating new dataset with " + size + " instances");
      data = new Instances("Train Incomplete", IncompleteAttributes.ATTRIBUTES, size);
    }
    
    List<Trainer> trainers = new ArrayList<Trainer>();
    
    for (double m: misses) {
      
      System.out.println("Creating trainer " + m);
      Trainer trainer = new Trainer(m);
      trainers.add(trainer);
      trainer.start();
      
//      for (int sampleSize: sampleSizes) {            
//        for (double phi: phis) {
//          long start = System.currentTimeMillis();
//          generateTrainData(elements, phi, sampleSize, IncompleteAttributes.RESAMPLE_SIZE, m, rep);
//          System.out.println(String.format("Miss: %2f, Sample size: %d, Phi: %2f in %d sec", m, sampleSize, phi, (System.currentTimeMillis() - start) / 1000));
//        }
//        write();
//      }
    }
    
    DataWriter writer = new DataWriter(30000);
    writer.start();
    
    for (Trainer trainer: trainers) {
      trainer.join();
    }
    writer.end();
    write();
  }
  
  private void generateTrainData(ElementSet elements, double phi, int sampleSize, int resampleSize, double missing, int rep) {
    for (int i = 0; i < rep; i++) {
      Instance instance = new DenseInstance(ATTRIBUTES.size());
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_ELEMENTS), elements.size());
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_REAL_PHI), phi);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sampleSize);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_RESAMPLE_SIZE), resampleSize);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_MISSING), missing);


      // Sample
      MallowsTriangle triangle = new MallowsTriangle(elements.getReferenceRanking(), phi);
      RIMRSampler sampler = new RIMRSampler(triangle);
      Sample sample = sampler.generate(sampleSize);
      Comb.comb(sample, missing);


      // triangle no row
      {
        SampleTriangle st = new SampleTriangle(sample);
        RIMRSampler resampler = new RIMRSampler(st);
        Sample resample = resampler.generate(resampleSize);
        DirectReconstructor rec = new DirectReconstructor();
        MallowsModel mallows = rec.reconstruct(resample);
        instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_NO_ROW), mallows.getPhi());
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


      instance.setDataset(data);
      
      synchronized (data) {
        data.add(instance);
      }
    }      
  }
  
  public static void main(String[] args) throws Exception {
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    File file = new File(folder, "incomplete.train.arff");
    IncompleteTrainer trainer = new IncompleteTrainer(file);
    trainer.buildDataset();
  }


  
  
  private class Trainer extends Thread {
    
    private double miss;
    
    private Trainer(double miss) {
      this.miss = miss;
      
    }

    @Override
    public void run() {
      System.out.println("Running trainer: miss = " + miss);
      for (int sampleSize: sampleSizes) {            
        for (double phi: phis) {
          long start = System.currentTimeMillis();
          generateTrainData(elements, phi, sampleSize, IncompleteAttributes.RESAMPLE_SIZE, miss, rep);
          System.out.println(String.format("Miss: %2f, Sample size: %d, Phi: %2f in %d sec", miss, sampleSize, phi, (System.currentTimeMillis() - start) / 1000));
        }
      }
    }
  }
  
  
  private class DataWriter extends Thread {
    
    private boolean running = false;
    private long sleep;
    
    private DataWriter(long sleep) {
      this.sleep = sleep;
    }
    
    public void run() {
      running = true;
      try {
        while (running) {
          Thread.sleep(sleep);
          System.out.println("Writing data...");
          write();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    private void end() {
      this.running = false;
    }
  }
  
}
