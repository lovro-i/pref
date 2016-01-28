package edu.drexel.cs.db.rank.top;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.SampleCompleter;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.incomplete.Missing;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangleByRow;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import edu.drexel.cs.db.rank.util.TrainUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class TopIncompleteGenerator {

  
  private final ArrayList<Attribute> attributes;  
  private final Instances data;
  private final boolean triangle;
  private final boolean triangleByRow;
  private final int boots;
  private int resampleSize = 10000;
  private double[] phis = TrainUtils.step(0, 1, 0.05);
  
  public TopIncompleteGenerator(boolean triangle, boolean triangleByRow, int bootstraps) throws Exception {
    if (!triangle && !triangleByRow && bootstraps == 0) throw new IllegalArgumentException("You must set at least one learner");
    
    this.triangle = triangle;
    this.triangleByRow = triangleByRow;
    this.boots = bootstraps;
    this.attributes = TopIncompleteAttributes.getAttributes(triangle, triangleByRow, boots);
    this.data = new Instances("Train Top Incomplete", attributes, 1);
  }
  
  public void setResampleSize(int size) {
    this.resampleSize = size;
  }
  
  public Instances getInstances() {
    return data;
  }
      
  public synchronized void add(Instance instance) {
    instance.setDataset(data);
    data.add(instance);
  }
  
  public void setTrainPhiStep(double phiStep) {
    this.phis = TrainUtils.step(0, 1, phiStep);
  }
  
  public ArrayList<Attribute> getAttributes() {
    return attributes;
  }
  
  public Instance generateInstance(ItemSet items, int sampleSize, int resampleSize, double phi, Tops tops) {
    Instance instance = new DenseInstance(attributes.size());
    instance.setValue(attributes.indexOf(TopIncompleteAttributes.ATTRIBUTE_REAL_PHI), phi);

    // Synthetic Sample
    Ranking center = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(center, phi);
    MallowsTriangle mallowsTriangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(mallowsTriangle);
    Sample sample = sampler.generate(sampleSize);
    tops.remove(sample);
    
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    
    // triangle no row
    if (triangle) {
      long start = System.currentTimeMillis();
      SampleTriangle st = new SampleTriangle(center, sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      Logger.info("SampleTriangle reconstructed phi %.3f as %.3f in %.1f sec", phi, mallows.getPhi(), 0.001d * (System.currentTimeMillis() - start));
      instance.setValue(attributes.indexOf(TopIncompleteAttributes.ATTRIBUTE_TRIANGLE_NO_ROW), mallows.getPhi());
    }


    // triangle by row
    if (triangleByRow) {
      long start = System.currentTimeMillis();
      SampleTriangleByRow st = new SampleTriangleByRow(model.getCenter(), sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      Logger.info("SampleTriangleByRow reconstructed phi %.3f as %.3f in %.1f sec", phi, mallows.getPhi(), 0.001d * (System.currentTimeMillis() - start));
      instance.setValue(attributes.indexOf(TopIncompleteAttributes.ATTRIBUTE_TRIANGLE_BY_ROW), mallows.getPhi());
    }

    // Completer
    if (boots > 0) {
      double[] bootstraps = new double[boots];
      for (int j = 0; j < bootstraps.length; j++) {
        TopSampleCompleter completer = new TopSampleCompleter(sample);
        Sample resample = completer.complete(1);
        MallowsModel mallows = reconstructor.reconstruct(resample, center);
        bootstraps[j] = mallows.getPhi();
      }
      instance.setValue(attributes.indexOf(TopIncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
      instance.setValue(attributes.indexOf(TopIncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));
    }

    return instance;
  }
  
  private ItemSet items;
  private int sampleSize;
  private Tops tops;
  private Queue<Double> queue = new LinkedBlockingQueue<Double>();
    
  
  /** Generate regression training data based on sample */
  public Instances generate(Sample sample, int reps, int threads) throws Exception {
    long start = System.currentTimeMillis();
    
    this.items = sample.getItemSet();
    this.sampleSize = sample.size();
    this.tops = new Tops(sample);
    
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
        
        Logger.info("Trainer #%d, phi %.2f", id, phi);
        
        Instance instance = generateInstance(items, sampleSize, resampleSize, phi, tops);
        add(instance);
      }
      Logger.info("Trainer #%d finished in %d sec", id, (System.currentTimeMillis() - start) / 1000);
    }
  }
  
}
