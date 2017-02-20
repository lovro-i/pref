package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.core.Preference;


public class UpperBoundCount extends UpperBound {

  public UpperBoundCount(Expander expander) {
    super(expander);
  }

  @Override
  public double getUpperBoundModifier(int step) {
    int d = 0;
    for (Preference pref: expander.getTransitiveClosure().getPreferences()) {
      int i = expander.getReferenceIndex(pref.higher);
      int j = expander.getReferenceIndex(pref.lower);
      if ((i > j) && (i > step || j > step)) d++;
      
    }
    return Math.pow(expander.getModel().getPhi(), d);
  }
  
  
  
}
