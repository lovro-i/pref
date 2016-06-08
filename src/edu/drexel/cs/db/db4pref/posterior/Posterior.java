package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;


public interface Posterior {

  public MallowsModel getModel();
  
  public double getProbability(Ranking r);  
  public double getProbability(PreferenceSet pref);
  public double getProbability(Sequence seq);
  
  
}
