package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.incomplete.AMPxSReconstructor;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;


public class AMPxSMixtureReconstructor {

  private int iterations;
  private double alpha;
  
  public AMPxSMixtureReconstructor(int iterations, double alpha) {
    this.iterations = iterations;
    this.alpha = alpha;
  }
  
  public MallowsMixtureModel reconstruct(ClusteringResult clusters) throws Exception {
    MallowsMixtureModel model = new MallowsMixtureModel(clusters.getItemSet());
    for (PreferenceSet exemplar: clusters.samples.keySet()) {
      Sample<PreferenceSet> sample = clusters.samples.get(exemplar);
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
