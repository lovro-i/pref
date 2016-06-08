package edu.drexel.cs.db.db4pref.mixture;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.db4pref.sampler.MallowsUtils;


public class MallowsMixtureTest {

  public static void main(String[] args) throws Exception {
    ItemSet items = new ItemSet(20);
    MallowsMixtureModel mmm = new MallowsMixtureModel(items);
    
    MallowsModel m1 = new MallowsModel(items.getRandomRanking(), 0.3);
    MallowsModel m2 = new MallowsModel(items.getRandomRanking(), 0.5);
    MallowsModel m3 = new MallowsModel(items.getRandomRanking(), 0.7);
    
    mmm.add(m1, 3);
    mmm.add(m2, 5);
    mmm.add(m3, 2);
    
    RankingSample sample = MallowsUtils.sample(mmm, 4000);
    
    
    PolynomialReconstructor rec = new PolynomialReconstructor();
    GenericMixtureReconstructor rec1 = new GenericMixtureReconstructor(rec, 10);
    
    MallowsMixtureModel mrec = rec1.reconstruct(sample);
    System.out.println(mrec);
    
  }
}
