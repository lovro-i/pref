package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.distance.PreferenceSimilarity;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureReconstructor.ClusteringResult;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Histogram;
import edu.drexel.cs.db.rank.util.Logger;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MallowsMixtureClusterer {

  private int maxClusters;
  private double alphaDecay = 0.65d; // 0.65d // smaller alphaDecay, more clusters; bigger alpha, more agressive clustering. 0.65 is OK
  
  public MallowsMixtureClusterer(int maxClusters) {
    this.maxClusters = maxClusters;
  }
  
  
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
}
