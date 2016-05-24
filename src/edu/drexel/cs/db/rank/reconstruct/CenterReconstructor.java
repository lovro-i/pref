package edu.drexel.cs.db.rank.reconstruct;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.rank.kemeny.KemenyCandidate;
import edu.drexel.cs.db.rank.core.PreferenceSet;

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
