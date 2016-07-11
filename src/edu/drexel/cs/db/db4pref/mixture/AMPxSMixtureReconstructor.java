package edu.drexel.cs.db.db4pref.mixture;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.reconstruct.incomplete.AMPxSReconstructor;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.reconstruct.CenterReconstructor;

/** Uses AMPxSReconstructor to model clusters into Mixture of Mallows Model */
public class AMPxSMixtureReconstructor implements MallowsMixtureReconstructor {

  private final int iterations;
  private final double alpha;
  
  public AMPxSMixtureReconstructor() {
    this(100, 0.1);
  }
  
  public AMPxSMixtureReconstructor(int iterations, double alpha) {
    this.iterations = iterations;
    this.alpha = alpha;
  }
  
  @Override
  public MallowsMixtureModel model(PreferenceClusters clusters) throws Exception {
    MallowsMixtureModel model = new MallowsMixtureModel(clusters.getItemSet());
    for (PreferenceSet exemplar: clusters.clusters.keySet()) {
      Sample<PreferenceSet> sample = clusters.clusters.get(exemplar);
      Ranking candidate = (exemplar instanceof Ranking) ? (Ranking) exemplar : sample.getItemSet().getRandomRanking();
      Ranking center = CenterReconstructor.reconstruct(sample, candidate);
      
      MallowsModel initial = new MallowsModel(center, 0d);
      AMPxSReconstructor reconstructor = new AMPxSReconstructor(initial, iterations, alpha);
      MallowsModel mm = reconstructor.reconstruct(sample, center);
      
      model.add(mm, sample.sumWeights());
    }
    return model;
  }


}
