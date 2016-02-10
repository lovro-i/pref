package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import java.util.ArrayList;
import java.util.List;


/** Sampler -- Give it a MallowsMixtureModel as input, and generate yourself some samples */
public class MallowsMixtureSampler {

  private MallowsMixtureModel model;
  
  public MallowsMixtureSampler(MallowsMixtureModel model) {
    this.model = model;
  }
  
  
  /** @return Sample of specified size, sampled from the mixture of models */
  public Sample generate(int size) {
    Sample sample = new Sample(model.getItemSet());
    
    // Create samplers for each model
    final List<RIMRSampler> samplers = new ArrayList<RIMRSampler>();
    for (MallowsModel mm: model.getModels()) {
      MallowsTriangle triangle = new MallowsTriangle(mm);
      RIMRSampler sampler = new RIMRSampler(triangle);
      samplers.add(sampler);
    }
              
    // Pick a random model and sample a ranking from it
    for (int i = 0; i < size; i++) {
      int index = model.getRandomModel();
      RIMRSampler sampler = samplers.get(index);
      Ranking r = sampler.generate();
      sample.add(r);
    }
    
    return sample;
  }
  
}
