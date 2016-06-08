package edu.drexel.cs.db.db4pref.mixture;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.sampler.RIMSampler;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.sampler.triangle.MallowsTriangle;
import java.util.ArrayList;
import java.util.List;


/** Sampler -- Give it a MallowsMixtureModel as input, and generate yourself some samples */
public class MallowsMixtureSampler {

  private MallowsMixtureModel model;
  
  public MallowsMixtureSampler(MallowsMixtureModel model) {
    this.model = model;
  }
  
  
  /** @return Sample of specified size, sampled from the mixture of models */
  public RankingSample sample(int size) {
    RankingSample sample = new RankingSample(model.getItemSet());
    
    // Create samplers for each model
    final List<RIMSampler> samplers = new ArrayList<RIMSampler>();
    for (MallowsModel mm: model.getModels()) {
      MallowsTriangle triangle = new MallowsTriangle(mm);
      RIMSampler sampler = new RIMSampler(triangle);
      samplers.add(sampler);
    }
              
    // Pick a random model and sample a ranking from it
    for (int i = 0; i < size; i++) {
      int index = model.getRandomModel();
      RIMSampler sampler = samplers.get(index);
      Ranking r = sampler.sample();
      sample.add(r);
    }
    
    return sample;
  }
  
}
