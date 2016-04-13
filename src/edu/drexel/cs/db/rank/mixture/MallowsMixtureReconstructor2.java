package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.distance.PreferenceSimilarity;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Variant of MallowsMixtureReconstructor which handles too much clusters (more than maxClusters) in a different way */
public class MallowsMixtureReconstructor2 {

  private int maxClusters = 10;
  private MallowsReconstructor reconstructor;

  
  public MallowsMixtureReconstructor2(MallowsReconstructor reconstructor) {
    this.reconstructor = reconstructor;
  }
      
  public MallowsMixtureReconstructor2(MallowsReconstructor reconstructor, int maxClusters) {
    this.reconstructor = reconstructor;
    this.maxClusters = maxClusters;
  }


  public ClusteringResult cluster(Sample<? extends PreferenceSet> sample) {
    double alpha = 0;
    while (true) {
      ClusteringResult result = cluster(sample, alpha);
      Logger.info("=========================================== Clustering with alpha %f: %d clusters", alpha, result.samples.size());

      try {
        FileOutputStream out = new FileOutputStream(new File("c:/temp/clresult.sav"));  
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(result);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      
      
      if (result.samples.size() <= maxClusters) return result;
      alpha += 1d;
    }
  }
  
  
  private double[][] matrix;
  private double minSim;
  private double maxSim;
  private List<PreferenceSet> preferences;
  private Map<PreferenceSet, Double> weights;
  
  private void calcSimilarities(Sample<? extends PreferenceSet> sample) {
    long start = System.currentTimeMillis();
    Histogram<PreferenceSet> hist = new Histogram<PreferenceSet>();
    hist.add(sample.preferenceSets(), sample.weights());
    weights = hist.getMap();
    preferences = new ArrayList<PreferenceSet>();
    preferences.addAll(weights.keySet());
    System.out.println(String.format("There are %d different rankings out of %d total rankings", preferences.size(), sample.size()));

    minSim = Double.POSITIVE_INFINITY;
    maxSim = Double.NEGATIVE_INFINITY;

    /* Create similarity matrix */      
    matrix = new double[preferences.size()][preferences.size()];

    Map<PreferenceSet, PreferenceSet> transitiveClosures = new HashMap<PreferenceSet, PreferenceSet>();
    for (PreferenceSet r : preferences) {
      transitiveClosures.put(r, r.transitiveClosure());
    }

    double lastPercent = -20;
    int done = 0;
    double total100 = 200d / (matrix.length * (matrix.length - 1));
    for (int i = 0; i < matrix.length; i++) {

      double percent = total100 * done;
      if (percent > lastPercent + 10) {
        System.out.print(String.format("%.0f%% ", percent));
        lastPercent = percent;
      }

      PreferenceSet mapPairsOneUser = preferences.get(i);

      // Similarities
      for (int j = i + 1; j < matrix.length; j++) {
        double s = PreferenceSimilarity.similarity(transitiveClosures.get(mapPairsOneUser), transitiveClosures.get(preferences.get(j)));
        maxSim = Math.max(maxSim, s);
        minSim = Math.min(minSim, s);
        matrix[i][j] = matrix[j][i] = s;
        done++;
      }
    }
    
    Logger.info("Similarity: [%f, %f]", minSim, maxSim);
    Logger.info("Similarity matrix %d x %d created in %d ms", matrix.length, matrix.length, System.currentTimeMillis() - start);
  }
  
  private ClusteringResult cluster(Sample<? extends PreferenceSet> sample, double alpha) {
    if (matrix == null) calcSimilarities(sample);

    double pref = minSim - alpha * 5; //maxSim;
    for (int i = 0; i < matrix.length; i++) {
      matrix[i][i] = pref;
    }
    Logger.info("Preferences for alpha %.1f set to %.1f", alpha, pref);

    /* Run Affinity Propagation */
    DataProvider provider = new MatrixProvider(matrix);
    // provider.addNoise();
    int groupCount = Math.min(8, matrix.length);
    Apro apro = new Apro(provider, groupCount, false);
    apro.run(100);

    /* Get exemplars for each mapPairsOneUser */
    Map<PreferenceSet, PreferenceSet> exemplars = new HashMap<>();
    Map<PreferenceSet, Sample<PreferenceSet>> samples = new HashMap<>(); // a sample for each exemplar
    for (int i = 0; i < preferences.size(); i++) {
      PreferenceSet r = preferences.get(i);
      int exi = apro.getExemplar(i);
      PreferenceSet exemplar = (exi != -1) ? preferences.get(exi) : r;
      exemplars.put(r, exemplar);

      // put it in the sample      
      Sample<PreferenceSet> s = samples.get(exemplar);
      if (s == null) {
        s = new Sample<PreferenceSet>(sample.getItemSet());
        samples.put(exemplar, s);
      }
      s.add(r, weights.get(r));
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
      MallowsModel mm = reconstructor.reconstruct(s, center);
      model.add(mm, s.sumWeights());
      Logger.info("Model %d of %d: %s", m, clustering.samples.size(), mm);
    }
    return model;
  }

  public int getMaxClusters() {
    return this.maxClusters;
  }

  public static class ClusteringResult implements Serializable {

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
