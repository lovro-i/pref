package edu.drexel.cs.db.db4pref.mixture;

/** Once the sample is clustered, use MallowsMixtureReconstructor to model clusters into MallowsMixtureModel */
public interface MallowsMixtureReconstructor {

  public abstract MallowsMixtureModel model(PreferenceClusters clustering) throws Exception;
  
}
