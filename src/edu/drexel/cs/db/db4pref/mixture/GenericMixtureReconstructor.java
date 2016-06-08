package edu.drexel.cs.db.db4pref.mixture;

import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.db4pref.kemeny.KemenyCandidate;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.db4pref.util.Logger;


/** Uses specified MallowsReconstructor (for a single model) to model clusters into Mixture of Mallows */
public class GenericMixtureReconstructor implements MallowsMixtureReconstructor {

  
  private int maxClusters = 10;
  private MallowsReconstructor reconstructor;
  

  public GenericMixtureReconstructor(MallowsReconstructor reconstructor) {
    this.reconstructor = reconstructor;
  }
  
  public GenericMixtureReconstructor(MallowsReconstructor reconstructor, int maxClusters) {
    this.reconstructor = reconstructor;
    this.maxClusters = maxClusters;
  }


  public PreferenceClusters cluster(Sample<? extends PreferenceSet> sample) {
    PreferenceClusterer clusterer = new PreferenceClusterer(maxClusters);
    return clusterer.cluster(sample);
  }

  public MallowsMixtureModel reconstruct(Sample<? extends PreferenceSet> sample) throws Exception {
    PreferenceClusters clustering = cluster(sample);
    return model(clustering);
  }

  /**
   * Now reconstruct each model from ClusteringResult
   */
  public MallowsMixtureModel model(PreferenceClusters clustering) throws Exception {
    MallowsMixtureModel model = new MallowsMixtureModel(clustering.getItemSet());
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
    int m = 0;
    for (PreferenceSet exemplar : clustering.clusters.keySet()) {
      m++;
      Logger.info("Reconstructing model %d of %d", m, clustering.clusters.size());
      Sample<? extends PreferenceSet> s = clustering.clusters.get(exemplar);
      Ranking center = KemenyCandidate.complete(exemplar);
      center = kemenizator.kemenize(s, center);
      MallowsModel mm = reconstructor.reconstruct(s, center);
      model.add(mm, s.sumWeights());
      Logger.info("Model %d of %d: %s", m, clustering.clusters.size(), mm);
    }
    return model;
  }

  public int getMaxClusters() {
    return this.maxClusters;
  }

}
