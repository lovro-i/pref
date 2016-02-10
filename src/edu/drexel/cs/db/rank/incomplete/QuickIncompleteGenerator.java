package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import static edu.drexel.cs.db.rank.incomplete.QuickIncompleteAttributes.ATTRIBUTES;
import edu.drexel.cs.db.rank.util.TrainUtils;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.filter.SampleCompleter;
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

@Deprecated
public class QuickIncompleteGenerator {
  
  private File arff;
  private Instances data;
  private int boots = IncompleteAttributes.BOOTSTRAPS;
  
  double[] phis = TrainUtils.step(0, 0.95, 0.05);
  

  
  
  public QuickIncompleteGenerator(File arff) throws Exception {
    this.arff = arff;
    if (arff.exists() && arff.length() > 0) {
      Logger.info("Loading existing dataset");
      InputStream is = new FileInputStream(arff);
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
      data = source.getDataSet();
      Logger.info("Loaded %d instances", data.size());
    }
    else {
      data = new Instances("Train Incomplete", QuickIncompleteAttributes.ATTRIBUTES, 1);
    }
  }

  
  public void setBootstraps(int bootstraps) {
    this.boots = bootstraps;
  }
  
  
  private long lastSave = 0;
  
  public synchronized void write() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(arff));
    writer.write(data.toString());
    writer.close();
    lastSave = System.currentTimeMillis();
  }
  
  public synchronized void checkWrite(long time) throws IOException {
    if (System.currentTimeMillis() - lastSave > time) write();
  }
  
  

  
  
  public Instance generateInstance(ItemSet items, int sampleSize, double phi, double missing) {
    Instance instance = new DenseInstance(ATTRIBUTES.size());
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_ITEMS), items.size());
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_SAMPLE_SIZE), sampleSize);
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_REAL_PHI), phi);
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_MISSING), missing);


    // Sample
    Ranking center = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(center, phi);
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    Filter.remove(sample, missing);
    
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    

    // Completer
    long start = System.currentTimeMillis();
    double[] bootstraps = new double[boots];
    System.out.println(boots);
    for (int j = 0; j < bootstraps.length; j++) {
      SampleCompleter completer = new SampleCompleter(sample);
      Sample resample = completer.complete(1);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      bootstraps[j] = mallows.getPhi();
    }
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
    instance.setValue(ATTRIBUTES.indexOf(QuickIncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));


    return instance;
  }
  
  public synchronized void add(Instance instance) {
    instance.setDataset(data);
    data.add(instance);
  }
  
  /** Generates training examples with parameters from the sample: sampleSize, number of items, missing rate
   * Iterates through <i>phi</i>s, and repeats it <i>reps</i> times for every <i>phi</i>
   * 
   * @param sample Sample to generate similar ones
   * @param reps Number of instances to generate per every phi
   */
  public void generate(Sample sample, int reps) throws Exception {
    long start = System.currentTimeMillis();
    int count = 0;
    double missing = IncompleteUtils.getMissingRate(sample);    
    for (int i = 0; i < reps; i++) {
      for (double phi: phis) {
        Logger.info("Training pass %d, phi %.2f", i, phi);
        Instance instance = generateInstance(sample.getItemSet(), sample.size(), phi, missing);
        add(instance);
        count++;
      }
      write();
    }
    Logger.info("%d instances generated in %d sec", count, (System.currentTimeMillis() - start) / 1000);
  }
  
  
  
  
  /** Parallel implementation of method generate, with <code>reps</code> number of threads
   * 
   * @param sample Sample to generate similar ones (size, number of items, missing rate)
   * @param reps Number of threads per phi
   */
  public void generateParallel(Sample sample, int reps) throws Exception {
    Logger.info("QuickGenerating regression training samples: %d items, %d rankings, %.3f missing rate", sample.getItemSet().size(), sample.size(), IncompleteUtils.getMissingRate(sample));
    long start = System.currentTimeMillis();
    
    //SystemOut.mute();
    List<Trainer> trainers = new ArrayList<Trainer>();
    for (int id = 1; id <= reps; id++) {
      Trainer trainer = new Trainer(sample);
      trainer.start();
      trainers.add(trainer);
    }
    
    for (Trainer trainer: trainers) trainer.join();
    
    write();
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
        if (id == 1) Logger.info("Trainer #%d, phi %.2f", id, phi);
        Instance instance = generateInstance(sample.getItemSet(), sample.size(), phi, missing);
        add(instance);
        try { checkWrite(5000); } 
        catch (IOException ex) { }
      }
      Logger.info("Trainer #%d finished in %d sec", id, (System.currentTimeMillis() - start) / 1000);
    }
  }
  
  
}
