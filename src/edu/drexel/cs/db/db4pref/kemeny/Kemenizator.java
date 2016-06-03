package edu.drexel.cs.db.db4pref.kemeny;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;

/** Interface implemented by different implementations of kemenizators */
public interface Kemenizator {

  public Ranking kemenize(Sample<? extends PreferenceSet> sample, Ranking start);
  
}
