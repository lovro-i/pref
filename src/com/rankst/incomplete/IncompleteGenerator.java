package com.rankst.incomplete;

import com.rankst.comb.Comb;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import static com.rankst.incomplete.IncompleteAttributes.ATTRIBUTES;
import com.rankst.ml.TrainUtils;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.DirectReconstructor;
import com.rankst.reconstruct.PolynomialReconstructor;
import com.rankst.sample.SampleCompleter;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import com.rankst.triangle.SampleTriangleByRow;
import com.rankst.util.FileUtils;
import com.rankst.util.Logger;
import com.rankst.util.MathUtils;
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
  
  
  private int[] sampleSizes = { 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000 };
  private double misses[] = TrainUtils.step(0, 0.75, 0.05);
  private double phis[] = TrainUtils.step(0.05, 0.8, 0.05);
  private int elems[] = { 5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 180, 200 };
  private int reps = 10;
  private int resampleSize = 5000;

  
  
  public IncompleteGenerator(File arff) throws Exception {
    this.arff = arff;
    if (arff.exists()) {
      Logger.info("Loading existing dataset");
      InputStream is = new FileInputStream(arff);
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
      data = source.getDataSet();
      Logger.info("Loaded %d instances", data.size());
      loadState();
      rewindState();
    }
    else {
      int size = phis.length * sampleSizes.length * misses.length * reps * elems.length;
      System.out.println("Creating new dataset with " + size + " instances");
      data = new Instances("Train Incomplete", IncompleteAttributes.ATTRIBUTES, size);
    }
  }
  
  private int si = 0;
  private int rep = 0;
  private int pi = 0;
  private int mi = 0;
  private int ei = 0;
  private ElementSet elements;
  
  private synchronized void saveState() throws IOException {
    Properties props = new Properties();
    props.setProperty("si", String.valueOf(si));
    props.setProperty("rep", String.valueOf(rep));
    props.setProperty("pi", String.valueOf(pi));
    props.setProperty("mi", String.valueOf(mi));
    props.setProperty("ei", String.valueOf(ei));
    PrintWriter out = FileUtils.write(new File(arff.getParentFile(), "incomplete.generator.state"));
    props.store(out, "IncompleteGenerator State");
    out.close();
  }
  
  private synchronized void loadState() throws IOException {
    Properties props = new Properties();
    File stateFile = new File(arff.getParentFile(), "incomplete.generator.state");
    if (!stateFile.exists()) return;
    
    FileReader reader = new FileReader(stateFile); 
    props.load(reader);
    
    this.si = Integer.parseInt(props.getProperty("si"));
    this.rep = Integer.parseInt(props.getProperty("rep"));
    this.pi = Integer.parseInt(props.getProperty("pi"));
    this.mi = Integer.parseInt(props.getProperty("mi"));
    this.ei = Integer.parseInt(props.getProperty("ei"));
    
    System.out.println("Continuing...");
  }
  
  private synchronized void rewindState() {
    if (pi > 0) pi--;
    si = 0;
    mi = 0;
  }
  
  private synchronized TrainInstance next() {
    si++;
    if (si == sampleSizes.length) {
      si = 0;
      mi++;
    }
    
    if (mi == misses.length) {
      mi = 0;
      pi++;
    }
    
    if (pi == phis.length) {
      pi = 0;
      ei++;
    }
    
    if (ei == elems.length) {
      ei = 0;
      rep++;
    }
    
    if (rep == reps) return null;
    if (elements == null || elements.size() != elems[ei]) elements = new ElementSet(elems[ei]);
    
    return new TrainInstance(elements, sampleSizes[si], resampleSize, phis[pi], misses[mi]);
  }
  
  private synchronized void write() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(arff));
    writer.write(data.toString());
    writer.close();
  }
  
  private long lastSave = 0;
  private long savePeriod = 1 * 60 * 1000;
  
  private synchronized void checkWrite() throws IOException {
    if (System.currentTimeMillis() - lastSave >= savePeriod) {
      System.out.println("Writing data...");
      write();
      saveState();
      lastSave = System.currentTimeMillis();
    }
  }
  
  
  
  
  
  private long lastLog = 0;
  private long logPeriod = 1 * 1000;
  
  private void log(TrainInstance ti) {
    if (System.currentTimeMillis() - lastLog >= logPeriod) {
      lastLog = System.currentTimeMillis();
      System.out.println(ti);      
    }
  }
  
  private int threads = 8;
  private List<Trainer> trainers = new ArrayList<Trainer>();
  
  private void generate() throws InterruptedException {
    for (int i = 0; i < threads; i++) {
      Trainer trainer = new Trainer(i);
      trainer.start();
      trainers.add(trainer);
    }
    
    for (Trainer trainer: trainers) trainer.join();    
  }
  
  
  private Instance generateInstance(ElementSet elements, int sampleSize, int resampleSize, double phi, double missing) {
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
    
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    
    // triangle no row
    {
      SampleTriangle st = new SampleTriangle(sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_NO_ROW), mallows.getPhi());
    }


    // triangle by row
    {
      SampleTriangleByRow st = new SampleTriangleByRow(sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample);
      instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_TRIANGLE_BY_ROW), mallows.getPhi());
    }

    // Completer
    double[] bootstraps = new double[IncompleteAttributes.BOOTSTRAPS];
    for (int j = 0; j < bootstraps.length; j++) {
      SampleCompleter completer = new SampleCompleter(sample);
      Sample resample = completer.complete(1);
      MallowsModel mallows = reconstructor.reconstruct(resample);
      bootstraps[j] = mallows.getPhi();
    }
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
    instance.setValue(ATTRIBUTES.indexOf(IncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));


    return instance;
  }
  
  private synchronized void add(Instance instance) {
    instance.setDataset(data);
    data.add(instance);
  }
  
  private class Trainer extends Thread {

    private int id;
    
    private Trainer(int id) {
      this.id = id;
    }

    @Override
    public void run() {
      System.out.println("Starting trainer " + id);
      TrainInstance ti = next();
      while (ti != null) {
        ti.train();
        log(ti);
        try { checkWrite(); }
        catch (IOException e) { e.printStackTrace(); }
        ti = next();
      }
    }
    
    @Override
    public String toString() {
      return "Trainer #"+id;
    }
  }
  
  private class TrainInstance {
    
    private double miss;
    private ElementSet elements;
    private int sampleSize;
    private int resampleSize;
    private double phi;

    @Override
    public String toString() {
      return String.format("n = %d, phi = %.2f, miss = %.2f, sample = %d", elements.size(), phi, miss, sampleSize);
    }
    
    
    private TrainInstance(ElementSet elements, int sampleSize, int resampleSize, double phi, double miss) {
      this.miss = miss;
      this.phi = phi;
      this.elements = elements;
      this.sampleSize = sampleSize;
      this.resampleSize = resampleSize;
    }

    public void train() {
      Instance instance = generateInstance(elements, sampleSize, resampleSize, phi, miss);
      add(instance);
    }
  }
  
  
  public static void main(String[] args) throws Exception {
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    IncompleteGenerator generator = new IncompleteGenerator(new File(folder, "incomplete.train.arff"));
    generator.generate();
  }
}
