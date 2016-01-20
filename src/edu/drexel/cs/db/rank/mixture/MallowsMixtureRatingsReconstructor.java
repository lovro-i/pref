package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.distance.RatingsSimilarity;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.rating.Ratings;
import edu.drexel.cs.db.rank.rating.RatingsSample;
import edu.drexel.cs.db.rank.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.rank.kemeny.KemenyCandidate;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.util.Logger;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.util.HashMap;
import java.util.Map;


public class MallowsMixtureRatingsReconstructor {

  private int maxClusters = 10;
  private MallowsReconstructor reconstructor;
  private double alphaDecay = 0.65d; // 0.65d // smaller alphaDecay, more clusters; bigger alpha, more agressive clustering. 0.65 seems OK
  private int maxRankings = 10;
  
  public MallowsMixtureRatingsReconstructor(MallowsReconstructor reconstructor) {
    this.reconstructor = reconstructor;
  }
  
  public MallowsMixtureRatingsReconstructor(MallowsReconstructor reconstructor, int maxClusters) {
    this.reconstructor = reconstructor;
    this.maxClusters = maxClusters;
  }
  
  /** Set maximum number of rankings per ratings when converting RatingsSample to rankings Sample */
  public void setMaxRankings(int maxRankings) {
    this.maxRankings = maxRankings;
  }
  
  private ClusteringResult cluster(RatingsSample sample, double alpha) {    
    double minSim = Double.POSITIVE_INFINITY;
    double maxSim = Double.NEGATIVE_INFINITY;
    
    /* Create similarity matrix */
    long start = System.currentTimeMillis();
    double[][] matrix = new double[sample.size()][sample.size()];
    Logger.info("Creating similarity matrix %d x %d", matrix.length, matrix.length);
    
    double lastPercent = -20;
    int count = 0;
    double nsquare100 = 200d / (matrix.length * (matrix.length - 1));
    for (int i = 0; i < matrix.length; i++) {
      
      double percent = nsquare100 * count;
      if (percent > lastPercent + 10) {
        System.out.print(String.format("%.0f%% ", percent));
        lastPercent = percent;
      }
      
      Ratings ratings = sample.get(i);
      matrix[i][i] = 1; // preference

      // Similarities
      for (int j = i+1; j < matrix.length; j++) {
        double s = RatingsSimilarity.similarity(ratings, sample.get(j));
        maxSim = Math.max(maxSim, s);
        minSim = Math.min(minSim, s);
        matrix[i][j] = matrix[j][i] = s;
        count++;
      }
    }
    
    for (int i = 0; i < matrix.length; i++) {
      matrix[i][i] = alpha * matrix[i][i] * minSim - (1 - alpha) * maxSim;
    }    
    
    Logger.info("\nMatrix %d x %d created in %.1f sec. Similarities: [%f, %f]", matrix.length, matrix.length, 0.01d * (System.currentTimeMillis()-start), minSim, maxSim);
    
    /* Run Affinity Propagation */
    DataProvider provider = new MatrixProvider(matrix);
    provider.addNoise();
    int groupCount = Math.min(8, matrix.length);
    Apro apro = new Apro(provider, groupCount, false);    
    apro.run(100);
    
    /* Get exemplars for each ranking */
    Map<Ratings, Ratings> exemplars = new HashMap<Ratings, Ratings>();
    Map<Ratings, RatingsSample> samples = new HashMap<Ratings, RatingsSample>(); // a sample for each exemplar
    for (int i = 0; i < sample.size(); i++) {
      Ratings r = sample.get(i);
      int exi = apro.getExemplar(i);
      Ratings exemplar = (exi != -1) ? sample.get(exi) : r;      
      exemplars.put(r, exemplar);
      
      // put it in the sample      
      RatingsSample s = samples.get(exemplar);
      if (s == null) {
        s = new RatingsSample(sample.getItems());
        samples.put(exemplar, s);
      }
      s.add(r, sample.getWeight(r));
    }
    
    /* If there are too much clusters, do it again */
    if (samples.size() > maxClusters && maxSim > 0) {
      RatingsSample more = new RatingsSample(sample.getItems());
      Logger.info("%d exemplars. Compacting more with alpha = %.3f...", samples.size(), alpha);
      for (Ratings r: samples.keySet()) {
        double w = samples.get(r).sumWeights();
        more.add(r, w);
      }
      
      ClusteringResult sub = cluster(more, alphaDecay * alpha);
      Map<Ratings, Ratings> newExs = new HashMap<Ratings, Ratings>();
      Map<Ratings, RatingsSample> newSamps = new HashMap<Ratings, RatingsSample>();
      for (Ratings r: sample.keySet()) {
        Ratings ex1 = exemplars.get(r);
        Ratings ex2 = sub.exemplars.get(ex1);
        newExs.put(r, ex2);
        
        RatingsSample s = newSamps.get(ex2);
        if (s == null) {
          s = new RatingsSample(sample.getItems());
          newSamps.put(ex2, s);
        }
        s.add(r);
      }
      
      exemplars = newExs;
      samples = newSamps;
    }
    
    return new ClusteringResult(exemplars, samples);
  }
  
  
  public MallowsMixtureModel reconstruct(RatingsSample sample) throws Exception {  
    ClusteringResult clustering = cluster(sample, 1d);
    return model(sample.getItems(), clustering);
  }
  
    
  /** Now reconstruct each model from ClusteringResult */
  private MallowsMixtureModel model(ItemSet items, ClusteringResult clustering) throws Exception {
    Logger.info("Modeling %d clusters", clustering.size());
    System.out.print("Cluster sizes:");
    for (RatingsSample s: clustering.samples.values()) {
      System.out.print(" " + s.size());
    }
    System.out.println();
    
    MallowsMixtureModel model = new MallowsMixtureModel(items);
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();    
    int m = 0;
    for (Ratings exemplar: clustering.samples.keySet()) {
      m++;
      Logger.info("Reconstructing model %d of %d", m, clustering.size());
      RatingsSample s = clustering.samples.get(exemplar);
      Ranking center = KemenyCandidate.complete(exemplar.toRanking());
      center = kemenizator.kemenize(s, center);
      MallowsModel mm = reconstructor.reconstruct(s.toSample(maxRankings), center);
      model.add(mm, s.sumWeights());
      
      Logger.info("Model so far [after %d of %d]:\n%s", m, clustering.size(), model);
    }
    return model;
  }

  public int getMaxClusters() {
    return this.maxClusters;
  }
  
  
  private class ClusteringResult {
    
    private final Map<Ratings, Ratings> exemplars;
    private final Map<Ratings, RatingsSample> samples;
    
    private ClusteringResult(Map<Ratings, Ratings> exemplars, Map<Ratings, RatingsSample> samples) {
      this.exemplars = exemplars;
      this.samples = samples;
    }
    
    private int size(){
      return samples.size();
    }
  }
}
