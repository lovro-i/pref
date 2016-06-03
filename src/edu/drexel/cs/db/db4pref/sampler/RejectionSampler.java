package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.triangle.MallowsTriangle;

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
      System.out.println(r);
      if (!r.isConsistent(pref)) r = null;
    }
    return r;
  }

  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    Ranking v = new Ranking(items);
    v.add(items.get(1));    
    v.add(items.get(2));
    v.add(items.get(3));
    v.add(items.get(4));
    System.out.println(v);
    
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.99);
    RejectionSampler sampler = new RejectionSampler(model);
    RankingSample sample = sampler.sample(v, 100);
    System.out.println(sample);
    
    
    AMPSampler amp = new AMPSampler(model);
    RankingSample sample1 = amp.sample(v, 100);
    //System.out.println(sample1);
  }
}
