package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.distance.PreferenceSimilarity;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.incomplete.AMPReconstructor;
import edu.drexel.cs.db.rank.incomplete.AMPxDIReconstructor;
import edu.drexel.cs.db.rank.incomplete.AMPxDReconstructor;
import edu.drexel.cs.db.rank.incomplete.AMPxINReconstructor;
import edu.drexel.cs.db.rank.incomplete.AMPxIReconstructor;
import edu.drexel.cs.db.rank.incomplete.AMPxNReconstructor;
import edu.drexel.cs.db.rank.incomplete.AMPxReconstructor;
import edu.drexel.cs.db.rank.incomplete.HybridReconstructor;
import edu.drexel.cs.db.rank.util.Histogram;
import edu.drexel.cs.db.rank.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.rank.kemeny.KemenyCandidate;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.util.Logger;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MallowsMixtureReconstructor {

  private int maxClusters = 10;
  private MallowsReconstructor reconstructor;
  private String reconstructorName;
  private int maxIterationEM = 100;
  private int alphaInAMPx = 1;
  private double initialPhi = 0.5;
  private double alphaDecay = 0.65d; // 0.65d // smaller alphaDecay, more clusters; bigger alpha, more agressive clustering. 0.65 is OK

    public MallowsMixtureReconstructor(MallowsReconstructor reconstructor) {
    this.reconstructor = reconstructor;
  }
    
  public MallowsMixtureReconstructor(String reconstructorName) {
    this.reconstructorName = reconstructorName;
  }
  
    public MallowsMixtureReconstructor(MallowsReconstructor reconstructor, int maxClusters) {
    this.reconstructor = reconstructor;
    this.maxClusters = maxClusters;
  }

  public MallowsMixtureReconstructor(String reconstructorName, int maxClusters) {
    this.reconstructorName = reconstructorName;
    this.maxClusters = maxClusters;
  }

  public MallowsMixtureReconstructor(String reconstructorName, int maxClusters, int maxIterationEM, int alphaInAMPx, double initialPhi) {
    this.reconstructorName = reconstructorName;
    this.maxClusters = maxClusters;
    this.maxIterationEM = maxIterationEM;
    this.alphaInAMPx = alphaInAMPx;
    this.initialPhi = initialPhi;
  }

//  public ClusteringResult cluster(Sample<PreferenceSet> sample) {
//    return cluster(sample, 1d);
//  }
//
//  private ClusteringResult cluster(Sample<PreferenceSet> sample, double alpha) {
//    Histogram<PreferenceSet> hist = new Histogram<PreferenceSet>();
//    hist.add(sample.preferenceSets(), sample.weights());
//    Map<PreferenceSet, Double> weights = hist.getMap();
//    List<PreferenceSet> rankings = new ArrayList<PreferenceSet>();
//    rankings.addAll(weights.keySet());
//    System.out.println(String.format("There are %d different rankings out of %d total rankings", rankings.size(), sample.size()));
//
//    double minSim = Double.POSITIVE_INFINITY;
//    double maxSim = Double.NEGATIVE_INFINITY;
//    double minPref = Double.POSITIVE_INFINITY;
//    double maxPref = Double.NEGATIVE_INFINITY;
//
//    /* Create similarity matrix */
//    long start = System.currentTimeMillis();
//    double[][] matrix = new double[rankings.size()][rankings.size()];
//
//    Map<PreferenceSet, PreferenceSet> transitiveClosures = new HashMap<PreferenceSet, PreferenceSet>();
//    for (PreferenceSet r : rankings) {
//      transitiveClosures.put(r, r.transitiveClosure());
//    }
//
//    double lastPercent = -20;
//    int done = 0;
//    double nsquare100 = 200d / (matrix.length * (matrix.length - 1));
//    for (int i = 0; i < matrix.length; i++) {
//
//      double percent = nsquare100 * done;
//      if (percent > lastPercent + 10) {
//        System.out.print(String.format("%.0f%% ", percent));
//        lastPercent = percent;
//      }
//
//      PreferenceSet ranking = rankings.get(i);
//      double pref = weights.get(ranking);
//      maxPref = Math.max(maxPref, pref);
//      minPref = Math.min(minPref, pref);
//      matrix[i][i] = pref;
//
//      // Similarities
//      for (int j = i + 1; j < matrix.length; j++) {
//        double s = PreferenceSimilarity.similarity(transitiveClosures.get(ranking), transitiveClosures.get(rankings.get(j)));
//        maxSim = Math.max(maxSim, s);
//        minSim = Math.min(minSim, s);
//        matrix[i][j] = matrix[j][i] = s;
//        done++;
//      }
//    }
//
//    for (int i = 0; i < matrix.length; i++) {
//      matrix[i][i] = alpha * matrix[i][i] * minSim - (1 - alpha) * maxSim;
//      //matrix[i][i] *= minSim;
//      //matrix[i][i] = matrix[i][i] - maxSim * (1 - alpha);
//    }
//
//    Logger.info("Pref: [%f, %f], Sim: [%f, %f]", minPref, maxPref, minSim, maxSim);
//    Logger.info("Matrix %d x %d created in %d ms", matrix.length, matrix.length, System.currentTimeMillis() - start);
//
//    /* Run Affinity Propagation */
//    DataProvider provider = new MatrixProvider(matrix);
//    provider.addNoise();
//    int groupCount = Math.min(8, matrix.length);
//    Apro apro = new Apro(provider, groupCount, false);
//    apro.run(100);
//
//    /* Get exemplars for each mapPairsOneUser */
//    Map<Ranking, Ranking> exemplars = new HashMap<Ranking, Ranking>();
//    Map<Ranking, RankingSample> samples = new HashMap<Ranking, RankingSample>(); // a sample for each exemplar
//    for (int i = 0; i < rankings.size(); i++) {
//      Ranking r = rankings.get(i);
//      int exi = apro.getExemplar(i);
//      Ranking exemplar = (exi != -1) ? rankings.get(exi) : r;
//      exemplars.put(r, exemplar);
//
//      // put it in the sample      
//      RankingSample s = samples.get(exemplar);
//      if (s == null) {
//        s = new RankingSample(sample.getItemSet());
//        samples.put(exemplar, s);
//      }
//      s.add(r, weights.get(r));
//    }
//
//    /* If there are too much clusters, do it again */
//    if (samples.size() > maxClusters && maxSim > 0) { // && samples.size() < sample.size()) {      
//      RankingSample more = new RankingSample(sample.getItemSet());
//      // alpha = 0.3 * Math.random();
//      Logger.info("%d exemplars. Compacting more with alpha = %.3f...", samples.size(), alpha);
//      for (Ranking r : samples.keySet()) {
//        double w = samples.get(r).sumWeights();
//        more.add(r, w);
//      }
//
//      ClusteringResult sub = cluster(more, alphaDecay * alpha);
//      Map<Ranking, Ranking> newExs = new HashMap<Ranking, Ranking>();
//      Map<Ranking, RankingSample> newSamps = new HashMap<Ranking, RankingSample>();
//      for (Ranking r : rankings) {
//        Ranking ex1 = exemplars.get(r);
//        Ranking ex2 = sub.exemplars.get(ex1);
//        newExs.put(r, ex2);
//
//        RankingSample s = newSamps.get(ex2);
//        if (s == null) {
//          s = new RankingSample(sample.getItemSet());
//          newSamps.put(ex2, s);
//        }
//        s.add(r, weights.get(r));
//      }
//
//      exemplars = newExs;
//      samples = newSamps;
//    }
//
//    return new ClusteringResult(exemplars, samples);
//  }
  public ClusteringResult cluster(Sample<? extends PreferenceSet> sample) {
    return cluster(sample, 1d);
  }

  private ClusteringResult cluster(Sample<? extends PreferenceSet> sample, double alpha) {
    Histogram<PreferenceSet> hist = new Histogram<PreferenceSet>();
    hist.add(sample.preferenceSets(), sample.weights());
    Map<PreferenceSet, Double> weights = hist.getMap();
    List<PreferenceSet> rankings = new ArrayList<PreferenceSet>();
    rankings.addAll(weights.keySet());
    System.out.println(String.format("There are %d different rankings out of %d total rankings", rankings.size(), sample.size()));

    double minSim = Double.POSITIVE_INFINITY;
    double maxSim = Double.NEGATIVE_INFINITY;
    double minPref = Double.POSITIVE_INFINITY;
    double maxPref = Double.NEGATIVE_INFINITY;

    /* Create similarity matrix */
    long start = System.currentTimeMillis();
    double[][] matrix = new double[rankings.size()][rankings.size()];

    Map<PreferenceSet, PreferenceSet> transitiveClosures = new HashMap<PreferenceSet, PreferenceSet>();
    for (PreferenceSet r : rankings) {
      transitiveClosures.put(r, r.transitiveClosure());
    }

    double lastPercent = -20;
    int done = 0;
    double nsquare100 = 200d / (matrix.length * (matrix.length - 1));
    for (int i = 0; i < matrix.length; i++) {

      double percent = nsquare100 * done;
      if (percent > lastPercent + 10) {
        System.out.print(String.format("%.0f%% ", percent));
        lastPercent = percent;
      }

      PreferenceSet mapPairsOneUser = rankings.get(i);
      double pref = weights.get(mapPairsOneUser);
      maxPref = Math.max(maxPref, pref);
      minPref = Math.min(minPref, pref);
      matrix[i][i] = pref;

      // Similarities
      for (int j = i + 1; j < matrix.length; j++) {
        double s = PreferenceSimilarity.similarity(transitiveClosures.get(mapPairsOneUser), transitiveClosures.get(rankings.get(j)));
        maxSim = Math.max(maxSim, s);
        minSim = Math.min(minSim, s);
        matrix[i][j] = matrix[j][i] = s;
        done++;
      }
    }

    for (int i = 0; i < matrix.length; i++) {
      matrix[i][i] = alpha * matrix[i][i] * minSim - (1 - alpha) * maxSim;
      //matrix[i][i] *= minSim;
      //matrix[i][i] = matrix[i][i] - maxSim * (1 - alpha);
    }

    Logger.info("Pref: [%f, %f], Sim: [%f, %f]", minPref, maxPref, minSim, maxSim);
    Logger.info("Matrix %d x %d created in %d ms", matrix.length, matrix.length, System.currentTimeMillis() - start);

    /* Run Affinity Propagation */
    DataProvider provider = new MatrixProvider(matrix);
    provider.addNoise();
    int groupCount = Math.min(8, matrix.length);
    Apro apro = new Apro(provider, groupCount, false);
    apro.run(100);

    /* Get exemplars for each mapPairsOneUser */
    Map<PreferenceSet, PreferenceSet> exemplars = new HashMap<>();
    Map<PreferenceSet, Sample<PreferenceSet>> samples = new HashMap<>(); // a sample for each exemplar
    for (int i = 0; i < rankings.size(); i++) {
      PreferenceSet r = rankings.get(i);
      int exi = apro.getExemplar(i);
      PreferenceSet exemplar = (exi != -1) ? rankings.get(exi) : r;
      exemplars.put(r, exemplar);

      // put it in the sample      
      Sample<PreferenceSet> s = samples.get(exemplar);
      if (s == null) {
        s = new Sample<PreferenceSet>(sample.getItemSet());
        samples.put(exemplar, s);
      }
      s.add(r, weights.get(r));
    }

    /* If there are too much clusters, do it again */
    if (samples.size() > maxClusters && maxSim > 0) { // && samples.size() < sample.size()) {      
      Sample<PreferenceSet> more = new Sample<>(sample.getItemSet());
      // alpha = 0.3 * Math.random();
      Logger.info("%d exemplars. Compacting more with alpha = %.3f...", samples.size(), alpha);
      for (PreferenceSet r : samples.keySet()) {
        double w = samples.get(r).sumWeights();
        more.add(r, w);
      }

      ClusteringResult sub = cluster(more, alphaDecay * alpha);
      Map<PreferenceSet, PreferenceSet> newExs = new HashMap<>();
      Map<PreferenceSet, Sample<PreferenceSet>> newSamps = new HashMap<>();
      for (PreferenceSet r : rankings) {
        PreferenceSet ex1 = exemplars.get(r);
        PreferenceSet ex2 = sub.exemplars.get(ex1);
        newExs.put(r, ex2);

        Sample<PreferenceSet> s = newSamps.get(ex2);
        if (s == null) {
          s = new Sample<PreferenceSet>(sample.getItemSet());
          newSamps.put(ex2, s);
        }
        s.add(r, weights.get(r));
      }

      exemplars = newExs;
      samples = newSamps;
    }

    return new ClusteringResult(exemplars, samples);
  }

  public MallowsMixtureModel reconstruct(Sample<? extends PreferenceSet> sample) throws Exception {
    ClusteringResult clustering = cluster(sample);
    return model(clustering);
  }

  /**
   * Now reconstruct each model from ClusteringResult
   */
  private MallowsMixtureModel model(ClusteringResult clustering) throws Exception {
    MallowsMixtureModel model = new MallowsMixtureModel(clustering.getItemSet());
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
    int m = 0;
    for (PreferenceSet exemplar : clustering.samples.keySet()) {
      m++;
      Logger.info("Reconstructing model %d of %d", m, clustering.samples.size());
      Sample<PreferenceSet> s = clustering.samples.get(exemplar);
      Ranking center = KemenyCandidate.complete(exemplar);
      center = kemenizator.kemenize(s, center);
      loadReconstructor(center);
      MallowsModel mm = reconstructor.reconstruct(s, center);
      model.add(mm, s.sumWeights());
      Logger.info("Model %d of %d: %s", m, clustering.samples.size(), mm);
    }
    return model;
  }

  public void loadReconstructor(Ranking center) {
    MallowsModel initialModel = new MallowsModel(center, 0.5);
    if (reconstructorName.equals("AMP")) {
      reconstructor = new AMPReconstructor(initialModel, maxIterationEM);
    } else if (reconstructorName.equals("AMPx")) {
      reconstructor = new AMPxReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-I")) {
      reconstructor = new AMPxIReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-D")) {
      reconstructor = new AMPxDReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-DI")) {
      reconstructor = new AMPxDIReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-D-I")) {
      reconstructor = new HybridReconstructor(initialModel, maxIterationEM, alphaInAMPx, false);
    } else if (reconstructorName.equals("AMPx-D-DI")) {
      reconstructor = new HybridReconstructor(initialModel, maxIterationEM, alphaInAMPx, true);
    } else if (reconstructorName.equals("AMPx-N")) {
      reconstructor = new AMPxNReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-IN")) {
      reconstructor = new AMPxINReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    }
  }

  public int getMaxClusters() {
    return this.maxClusters;
  }

  public class ClusteringResult {

    public final Map<PreferenceSet, PreferenceSet> exemplars;
    public final Map<PreferenceSet, Sample<PreferenceSet>> samples;

    private ClusteringResult(Map<PreferenceSet, PreferenceSet> exemplars, Map<PreferenceSet, Sample<PreferenceSet>> samples) {
      this.exemplars = exemplars;
      this.samples = samples;
    }

    public ItemSet getItemSet() {
      for (PreferenceSet r : exemplars.keySet()) {
        return r.getItemSet();
      }
      for (PreferenceSet r : samples.keySet()) {
        return r.getItemSet();
      }
      return null;
    }

  }

}
