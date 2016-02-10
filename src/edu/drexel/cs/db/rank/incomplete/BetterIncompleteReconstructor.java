package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.filter.SampleCompleter;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangleByRow;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.ArrayList;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class BetterIncompleteReconstructor implements MallowsReconstructor {

  private final boolean triangle;
  private final boolean triangleByRow;
  private final int boots;
  
  private int trainSeries = 3;
  private double phiStep = 0.05;
  private int threads = Runtime.getRuntime().availableProcessors();
  private int resampleSize = 10000;
  private int completions = 1;

  /** Use temp file for training instances just for this particular case */
  public BetterIncompleteReconstructor(boolean triangle, boolean triangleByRow, int bootstraps, int trainSeries) throws Exception {
    if (trainSeries < 1) throw new IllegalArgumentException("Number of train instances must be greater than zero");    
    if (!triangle && !triangleByRow && bootstraps == 0) throw new IllegalArgumentException("You must set at least one learner");
    
    this.triangle = triangle;
    this.triangleByRow = triangleByRow;
    this.boots = bootstraps;
    this.trainSeries = trainSeries;
  }
  
  /** Set the number of training series (phi from 0 to 1) to generate for regression learner */
  public void setTrainSeries(int series) {
    this.trainSeries = series;
  }
  
  /** Set the training step for phi */
  public void setTrainPhiStep(double phiStep) {
    this.phiStep = phiStep;  
  }
  
  /** Number of worker threads for regression training. Default is the number of cores */
  public void setTrainThreads(int threads) {
    this.threads = threads;
  }
  
  /** Get the number of regression training threads */
  public int getTrainThreads() {
    return threads;
  }
  
  /** Size of resample for triangle methods of reconstruction */
  public void setResampleSize(int resampleSize) {
    this.resampleSize = resampleSize;
  }
  
  public int getResampleSize() {
    return resampleSize;
  }
  
  
  /** How much completions per incomplete ranking to create */
  public void setCompletions(int completions) {
    this.completions = completions;
  }
  
  
  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) throws Exception {
    // File arff = File.createTempFile("train.", ".arff");
    // arff.deleteOnExit();    
    
    BetterIncompleteGenerator generator = new BetterIncompleteGenerator(triangle, triangleByRow, boots);
    generator.setTrainPhiStep(phiStep);
    generator.setResampleSize(resampleSize);
    Instances data = generator.generate(sample, trainSeries, threads);
    data.setClassIndex(data.numAttributes() - 1);    
    M5P classifier = new M5P();
    classifier.setMinNumInstances(4);    
    classifier.buildClassifier(data);
    
    ArrayList<Attribute> attributes = generator.getAttributes();
    Instance instance = new DenseInstance(attributes.size()); 
    if (center == null) center = CenterReconstructor.reconstruct(sample);
    PolynomialReconstructor reconstructor = new PolynomialReconstructor();
    
    // triangle no row
    if (triangle) {
      SampleTriangle st = new SampleTriangle(center, sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      instance.setValue(attributes.indexOf(BetterIncompleteAttributes.ATTRIBUTE_TRIANGLE_NO_ROW), mallows.getPhi());
    }


    // triangle by row
    if (triangleByRow) {
      SampleTriangleByRow st = new SampleTriangleByRow(center, sample);
      RIMRSampler resampler = new RIMRSampler(st);
      Sample resample = resampler.generate(resampleSize);
      MallowsModel mallows = reconstructor.reconstruct(resample, center);
      instance.setValue(attributes.indexOf(BetterIncompleteAttributes.ATTRIBUTE_TRIANGLE_BY_ROW), mallows.getPhi());
    }

    
    // Completer
    if (boots > 0) {
    double[] bootstraps = new double[boots];
      for (int j = 0; j < bootstraps.length; j++) {
        SampleCompleter completer = new SampleCompleter(sample);
        Sample resample = completer.complete(completions);
        MallowsModel mallows = reconstructor.reconstruct(resample, center);
        bootstraps[j] = mallows.getPhi();
      }
      instance.setValue(attributes.indexOf(BetterIncompleteAttributes.ATTRIBUTE_COMPLETER_MEAN), MathUtils.mean(bootstraps));
      instance.setValue(attributes.indexOf(BetterIncompleteAttributes.ATTRIBUTE_COMPLETER_VAR), MathUtils.variance(bootstraps));
    }
    
    double regressionPhi = classifier.classifyInstance(instance);
    return new MallowsModel(center, regressionPhi);
  }
  
  
 @Override
  public MallowsModel reconstruct(Sample sample) throws Exception {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }

  
  public static void main(String[] args) throws Exception {
    ItemSet items = new ItemSet(10);
    Sample sample = MallowsUtils.sample(items.getReferenceRanking(), 0.3, 5000);
    Filter.remove(sample, 0.6);
    
    BetterIncompleteReconstructor rec1 = new BetterIncompleteReconstructor(false, false, 10, 3);
    MallowsModel m1 = rec1.reconstruct(sample);
    System.out.println(m1);
  }
}
