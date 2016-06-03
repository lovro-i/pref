package edu.drexel.cs.db.db4pref.mixture;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.db4pref.kemeny.KemenyCandidate;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.io.Serializable;
import java.util.Map;

public class MallowsMixtureReconstructor {

  private int maxClusters = 10;
  private MallowsReconstructor reconstructor;
  

  public MallowsMixtureReconstructor(MallowsReconstructor reconstructor) {
    this.reconstructor = reconstructor;
  }
  
  public MallowsMixtureReconstructor(MallowsReconstructor reconstructor, int maxClusters) {
    this.reconstructor = reconstructor;
    this.maxClusters = maxClusters;
  }


  public ClusteringResult cluster(Sample<? extends PreferenceSet> sample) {
    MallowsMixtureClusterer clusterer = new MallowsMixtureClusterer(maxClusters);
    return clusterer.cluster(sample);
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
      Sample<? extends PreferenceSet> s = clustering.samples.get(exemplar);
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

    private static final long serialVersionUID = -6400584493142884637L;
    public final Map<PreferenceSet, PreferenceSet> exemplars;
    public final Map<PreferenceSet, Sample<PreferenceSet>> samples;

    ClusteringResult(Map<PreferenceSet, PreferenceSet> exemplars, Map<PreferenceSet, Sample<PreferenceSet>> samples) {
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
