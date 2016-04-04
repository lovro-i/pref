package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;

/** The ground-truth posterior Mallows sampler. Rejection sampler, thus may be slow */
public class RejectionSampler extends MallowsSampler {

  
  private RIMRSampler sampler;
    
  public RejectionSampler(MallowsModel model) {
    super(model);
  }

  @Override
  public void setModel(MallowsModel model) {
    this.model = model;
    MallowsTriangle triangle = new MallowsTriangle(model);
    this.sampler = new RIMRSampler(triangle);
  }
  
  
  @Override
  public Ranking sample(PreferenceSet pref) {
    Ranking r = null;
    while (r == null) {
      r = sampler.generate();
      if (!r.isConsistent(pref)) r = null;
    }
    return r;
  }

  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking v = new Ranking(items);
    v.add(items.get(3));    
    v.add(items.get(7));
    v.add(items.get(5));
    System.out.println(v);
    
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.1);
    RejectionSampler sampler = new RejectionSampler(model);
    RankingSample sample = sampler.sample(v, 10);
    System.out.println(sample);    
  }
}
