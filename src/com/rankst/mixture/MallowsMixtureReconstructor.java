package com.rankst.mixture;

import com.rankst.distance.RankingSimilarity;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.histogram.Histogram;
import com.rankst.kemeny.BubbleTableKemenizator;
import com.rankst.kemeny.KemenyCandidate;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.MallowsReconstructor;
import com.rankst.reconstruct.SmartReconstructor;
import com.rankst.util.Logger;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MallowsMixtureReconstructor {

  private int maxClusters = 10;
  private MallowsReconstructor reconstructor;
  
  public MallowsMixtureReconstructor(File arff) throws Exception {
    this(arff, 0);
  }
  
  public MallowsMixtureReconstructor(File arff, int trainReps) throws Exception {
    reconstructor = new SmartReconstructor(arff, trainReps);
  }
  
  
  public MallowsMixtureReconstructor(File arff, int trainReps, int maxClusters) throws Exception {    
    this(arff, trainReps);
    this.maxClusters = maxClusters;
  }
  
  
  @Deprecated
  public MallowsMixtureModel reconstruct1(Sample sample) throws Exception {
    return reconstructOld(sample, 1.7d);
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
      matrix[i][i] *= minSim;
    }    
    
    Logger.info("Pref: [%f, %f], Sim: [%f, %f]", minPref, maxPref, minSim, maxSim);
    Logger.info("Matrix %d x %d created in %d ms", matrix.length, matrix.length, System.currentTimeMillis()-start);
    
    /* Run Affinity Propagation */
    DataProvider provider = new MatrixProvider(matrix);
    provider.addNoise();
    Apro apro = new Apro(provider, 8, false);    
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
      //double alpha = 0.3 * Math.random();
      Logger.info("%d exemplars. Compacting more with alpha = %.3f...", samples.size(), alpha);
      for (Ranking r: samples.keySet()) {
        double w = alpha * samples.get(r).sumWeights();
        more.add(r, w);
      }
      
      ClusteringResult sub = cluster(more, 0.65d * alpha);
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
  
  

  private class ClusteringResult {
    
    private final Map<Ranking, Ranking> exemplars;
    private final Map<Ranking, Sample> samples;
    
    private ClusteringResult(Map<Ranking, Ranking> exemplars, Map<Ranking, Sample> samples) {
      this.exemplars = exemplars;
      this.samples = samples;
    }
  }
  
  @Deprecated
  public MallowsMixtureModel reconstructOld(Sample sample, double power) throws Exception {  
    ElementSet elements = sample.getElements();

    Histogram<Ranking> hist = new Histogram<Ranking>();
    hist.add(sample, sample.getWeights());
    Map<Ranking, Double> map = hist.getMap();
    List<Ranking> rankings = new ArrayList<Ranking>();
    rankings.addAll(map.keySet());
    System.out.println(String.format("There are %d different rankings out of %d total rankings", rankings.size(), sample.size()));
    
    /* Normalization parameters */    
    double normPref = hist.getMostFrequentCount();
    
    double minSim = Double.POSITIVE_INFINITY;
    double maxSim = Double.NEGATIVE_INFINITY;
    double minPref = Double.POSITIVE_INFINITY;
    double maxPref = Double.NEGATIVE_INFINITY;
    
    /* Create similarity matrix */
    long start = System.currentTimeMillis();
    double[][] matrix = new double[rankings.size()][rankings.size()];
    for (int i = 0; i < matrix.length; i++) {
      Ranking ranking = rankings.get(i);
      double pref = map.get(ranking);
      maxPref = Math.max(maxPref, pref);
      minPref = Math.min(minPref, pref);
      if (pref > 1) System.out.print(pref +", ");
      //else if (i % 10) System.out.println("");
      
      // Preference
      // if (power < 1.01) Logger.info("%.2f\t%.2f", sample.getWeight(ranking), map.get(ranking));
      matrix[i][i] = pref;
      // matrix[i][i] = map.get(ranking) / normPref - 1;
      // matrix[i][i] *= 100;

      // Similarities
      for (int j = i+1; j < matrix.length; j++) {
        double s = RankingSimilarity.similarity(ranking, rankings.get(j));
        maxSim = Math.max(maxSim, s);
        minSim = Math.min(minSim, s);
        matrix[i][j] = matrix[j][i] = s;
      }
    }
    
    // Normalize similarities    
//    for (int i = 0; i < matrix.length; i++) {
//      for (int j = i+1; j < matrix.length; j++) {
//        double s = matrix[i][j];
//        s = s / maxSim; // ne ovo, izleda: (s - minSim) / (maxSim - minSim);
//        matrix[i][j] = matrix[j][i] = s; // * s; // maxSim / (minSim + 1); 
//      }      
//    }
    
    Logger.info("\nPref: [%f, %f], Sim: [%f, %f]", minPref, maxPref, minSim, maxSim);
    
    double scale = Math.pow(elements.size(), power);
    for (int i = 0; i < matrix.length; i++) {
      matrix[i][i] *= minSim;
    }    
    Logger.info("Matrix %d x %d created in %d ms", matrix.length, matrix.length, System.currentTimeMillis()-start);
    
    
    /* Run Affinity Propagation */
    DataProvider provider = new MatrixProvider(matrix);
    provider.addNoise();
    Apro apro = new Apro(provider, 8, false);    
    apro.run(100);


    /* Get exemplars and separate samples */
    Map<Ranking, Sample> samples = new HashMap<Ranking, Sample>(); // a sample for each exemplar
    for (int i = 0; i < rankings.size(); i++) {
      Ranking r = rankings.get(i);
      int exi = apro.getExemplar(i);
      if (exi == -1) continue;
      Ranking exemplar = rankings.get(exi);
      Sample s = samples.get(exemplar);
      if (s == null) {
        s = new Sample(elements);
        samples.put(exemplar, s);
      }
      s.add(r, hist.get(r));
    }
    
    /* If there are too much clusters, do it again */
    if (samples.size() > maxClusters) {
      Logger.info("%d exemplars. Compacting more...", samples.size());
      Sample more = new Sample(sample.getElements());
      for (Ranking r: samples.keySet()) {
        double w = samples.get(r).sumWeights();
        more.add(r, w);
      }
      return this.reconstructOld(more, power * 0.7d);
    }

    /* Now reconstruct each model */
    MallowsMixtureModel model = new MallowsMixtureModel(elements);
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();    
    int m = 0;
    for (Ranking exemplar: samples.keySet()) {
      m++;
      Logger.info("Reconstructing model %d of %d", m, samples.size());
      Sample s = samples.get(exemplar);
      Ranking center = KemenyCandidate.complete(exemplar);
      center = kemenizator.kemenize(s, center);
      MallowsModel mm = reconstructor.reconstruct(s, center);
      model.add(mm, s.sumWeights());
    }
    return model;
  }
  

}
