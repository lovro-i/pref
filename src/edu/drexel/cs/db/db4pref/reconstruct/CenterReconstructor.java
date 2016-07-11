package edu.drexel.cs.db.db4pref.reconstruct;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.db4pref.kemeny.KemenyCandidate;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Sample.PW;

/** Reconstructs center even from the incomplete sample */
public class CenterReconstructor {

  public static Ranking reconstruct(Sample<? extends PreferenceSet> sample) {
    return reconstruct(sample, null);
  }
  
  /** Fills up the ranking with the missing items */
  public static Ranking fillUp(Ranking candidate, Sample<? extends PreferenceSet> sample) {
    Ranking r = new Ranking(candidate);
    for (PW<? extends PreferenceSet> pw: sample) {
      for (Item item: pw.p.getItems()) {
        if (!r.contains(item)) r.add(item);
      }
    }
    return r;
  }
  
  public static Ranking reconstruct(Sample<? extends PreferenceSet> sample, Ranking candidate) {
    if (candidate == null) candidate = KemenyCandidate.find(sample);
    candidate = fillUp(candidate, sample);
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
    return kemenizator.kemenize(sample, candidate);
  }
}
