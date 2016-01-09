package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.distance.RankingSimilarity;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.histogram.Histogram;
import edu.drexel.cs.db.rank.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.rank.kemeny.KemenyCandidate;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.reconstruct.SmartReconstructor;
import edu.drexel.cs.db.rank.util.Logger;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MallowsMixtureReconstructor {

  private int maxClusters = 10;
  private MallowsReconstructor reconstructor;
  private double alphaDecay = 0.65d; // 0.65d // smaller alphaDecay, more clusters; bigger alpha, more agressive clustering. 0.65 is OK
  
  public MallowsMixtureReconstructor(MallowsReconstructor reconstructor) {
    this.reconstructor = reconstructor;
  }
  
  public MallowsMixtureReconstructor(MallowsReconstructor reconstructor, int maxClusters) {
    this.reconstructor = reconstructor;
    this.maxClusters = maxClusters;
  }
  

  

    
  
  private ClusteringResult cluster(Sample sample, double alpha) {
    Histogram<Ranking> hist = new Histogram<Ranking>();
    hist.add(sample, sample.getWeights());
    Map<Ranking, Double> weights = hist.getMap();
    List<Ranking> rankings = new ArrayList<Ranking>();
    rankings.addAll(weights.keySet());
    System.out.println(String.format("There are %d different rankings out of %d total rankings", rankings.size(), sample.size()));
    
    double minSim = Double.POSITIVE_INFINITY;
    double maxSim = Double.NEGATIVE_INFINITY;
    double minPref = Double.POSITIVE_INFINITY;
    double maxPref = Double.NEGATIVE_INFINITY;
    
    /* Create similarity matrix */
    long start = System.currentTimeMillis();
    double[][] matrix = new double[rankings.size()][rankings.size()];
    for (int i = 0; i < matrix.length; i++) {
      Ranking ranking = rankings.get(i);
      double pref = weights.get(ranking);
      maxPref = Math.max(maxPref, pref);
      minPref = Math.min(minPref, pref);
      matrix[i][i] = pref;

      // Similarities
      for (int j = i+1; j < matrix.length; j++) {
        double s = RankingSimilarity.similarity(ranking, rankings.get(j));
        maxSim = Math.max(maxSim, s);
        minSim = Math.min(minSim, s);
        matrix[i][j] = matrix[j][i] = s;
      }
    }
        
    
    for (int i = 0; i < matrix.length; i++) {
      matrix[i][i] = alpha * matrix[i][i] * minSim - (1 - alpha) * maxSim;
      //matrix[i][i] *= minSim;
      //matrix[i][i] = matrix[i][i] - maxSim * (1 - alpha);
    }    
    
    Logger.info("Pref: [%f, %f], Sim: [%f, %f]", minPref, maxPref, minSim, maxSim);
    Logger.info("Matrix %d x %d created in %d ms", matrix.length, matrix.length, System.currentTimeMillis()-start);
    
    /* Run Affinity Propagation */
    DataProvider provider = new MatrixProvider(matrix);
    provider.addNoise();
    int groupCount = Math.min(8, matrix.length);
    Apro apro = new Apro(provider, groupCount, false);    
    apro.run(100);

    
    /* Get exemplars for each ranking */
    Map<Ranking, Ranking> exemplars = new HashMap<Ranking, Ranking>();
    Map<Ranking, Sample> samples = new HashMap<Ranking, Sample>(); // a sample for each exemplar
    for (int i = 0; i < rankings.size(); i++) {
      Ranking r = rankings.get(i);
      int exi = apro.getExemplar(i);
      Ranking exemplar = (exi != -1) ? rankings.get(exi) : r;      
      exemplars.put(r, exemplar);
      
      // put it in the sample      
      Sample s = samples.get(exemplar);
      if (s == null) {
        s = new Sample(sample.getElements());
        samples.put(exemplar, s);
      }
      s.add(r, weights.get(r));      
    }
    
    /* If there are too much clusters, do it again */
    if (samples.size() > maxClusters) { // && samples.size() < sample.size()) {      
      Sample more = new Sample(sample.getElements());
      // alpha = 0.3 * Math.random();
      Logger.info("%d exemplars. Compacting more with alpha = %.3f...", samples.size(), alpha);
      for (Ranking r: samples.keySet()) {
        double w = samples.get(r).sumWeights();
        more.add(r, w);
      }
      
      ClusteringResult sub = cluster(more, alphaDecay * alpha);
      Map<Ranking, Ranking> newExs = new HashMap<Ranking, Ranking>();
      Map<Ranking, Sample> newSamps = new HashMap<Ranking, Sample>();
      for (Ranking r: rankings) {
        Ranking ex1 = exemplars.get(r);
        Ranking ex2 = sub.exemplars.get(ex1);
        newExs.put(r, ex2);
        
        Sample s = newSamps.get(ex2);
        if (s == null) {
          s = new Sample(sample.getElements());
          newSamps.put(ex2, s);
        }
        s.add(r, weights.get(r));
      }
      
      exemplars = newExs;
      samples = newSamps;
    }
        
    return new ClusteringResult(exemplars, samples);
  }
  
  
  public MallowsMixtureModel reconstruct(Sample sample) throws Exception {  
    ClusteringResult clustering = cluster(sample, 1d);
    
    /* Now reconstruct each model */
    MallowsMixtureModel model = new MallowsMixtureModel(sample.getElements());
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();    
    int m = 0;
    for (Ranking exemplar: clustering.samples.keySet()) {
      m++;
      Logger.info("Reconstructing model %d of %d", m, clustering.samples.size());
      Sample s = clustering.samples.get(exemplar);
      Ranking center = KemenyCandidate.complete(exemplar);
      center = kemenizator.kemenize(s, center);
      MallowsModel mm = reconstructor.reconstruct(s, center);
      model.add(mm, s.sumWeights());
    }
    return model;
  }

  public int getMaxClusters() {
    return this.maxClusters;
  }
  
  

  private class ClusteringResult {
    
    private final Map<Ranking, Ranking> exemplars;
    private final Map<Ranking, Sample> samples;
    
    private ClusteringResult(Map<Ranking, Ranking> exemplars, Map<Ranking, Sample> samples) {
      this.exemplars = exemplars;
      this.samples = samples;
    }
  }
  

}