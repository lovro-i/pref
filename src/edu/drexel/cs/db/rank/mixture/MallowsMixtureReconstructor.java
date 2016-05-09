package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.rank.kemeny.KemenyCandidate;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.util.Logger;

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

}
