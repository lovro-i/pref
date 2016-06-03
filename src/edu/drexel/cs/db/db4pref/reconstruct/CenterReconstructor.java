package edu.drexel.cs.db.db4pref.reconstruct;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.db4pref.kemeny.KemenyCandidate;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;

/** Reconstructs center even from the incomplete sample */
public class CenterReconstructor {

  public static Ranking reconstruct(Sample<? extends PreferenceSet> sample) {
    return reconstruct(sample, null);
  }
  
  public static Ranking reconstruct(Sample<? extends PreferenceSet> sample, Ranking candidate) {
    if (candidate == null) candidate = KemenyCandidate.find(sample);
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
    return kemenizator.kemenize(sample, candidate);
  }
}
