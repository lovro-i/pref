package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import java.util.concurrent.TimeoutException;


public interface Posterior {

  public MallowsModel getModel();
  
  public double getProbability(Ranking r) throws TimeoutException;  
  public double getProbability(PreferenceSet pref) throws TimeoutException;
  public double getProbability(Sequence seq) throws TimeoutException;
  
  
}
