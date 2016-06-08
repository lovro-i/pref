package edu.drexel.cs.db.db4pref.filter;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.Sample.PW;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import java.security.SecureRandom;
import java.util.Random;

/** For removing items from rankings and samples */
public class Filter {
  
  private static Random random = new SecureRandom();

  /** Remove items from the ranking with probability p for removing each one. Destructive on ranking, changes the actual ranking */
  public static void removeItems(Ranking ranking, double p) {
    for (int i = ranking.length()-1; i >= 0; i--) {
      double flip = random.nextDouble();
      if (flip < p) ranking.remove(i);
    }
  }
  
  /** Remove items from the PrefrenceSet with probability p for removing each one. Destructive on ranking, changes the actual ranking */
  public static void removeItems(PreferenceSet pref, double p) {
    removeItems(pref, MissingProbabilities.uniform(pref.getItemSet(), p));
  }
  
  /** Remove items from the ranking with probabilities specified in Missing. Destructive on ranking, changes the actual ranking */
  public static void removeItems(PreferenceSet pref, MissingProbabilities m) {
    m.removeItems(pref);
  }
  
  public static void removeItems(Sample<? extends PreferenceSet> sample, MissingProbabilities m) {
    for (PW pw: sample) {
      Filter.removeItems(pw.p, m);
    }
  }
  
  
  /** Remove items from all rankings with probability p for removing each one. Destructive, changes the actual sample and its rankings */
  public static void removeItems(Sample<? extends PreferenceSet> sample, double p) {
    removeItems(sample, MissingProbabilities.uniform(sample.getItemSet(), p));
  }
  
  
  /** Remove pairs from the preferenceSet with probabilities specified in Missing. */
  public static void removePreferences(MutablePreferenceSet pref, MissingProbabilities m) {
    m.removePreferences(pref);
  }
  
  
  
  /** Leave only top K items in each ranking */
  public static void top(RankingSample sample, int k) {
    for (Ranking r: sample.rankings()) {
      while (r.length() > k) r.remove(r.length() - 1);
    }
  }
  
  /** Leave between min and max (both inclusive) items in the ranking. Uniform distribution */
  public static void top(RankingSample sample, int min, int max) {
    for (Ranking r: sample.rankings()) {
      int k = min + random.nextInt(max - min + 1);
      while (r.length() > k) r.remove(r.length() - 1);
    }
  }
  
  /** Replaces a ranking with a uniformly random one with probability p */
  public static void noise(RankingSample sample, double p) {
    for (Ranking r: sample.rankings()) {
      double flip = random.nextDouble();
      if (flip < p) randomize(r);
    }
  }
  
  /** Uniformly randomizes the items of the ranking. Destructive, changes the actual ranking */
  public static void randomize(Ranking ranking) {
    for (int i = 0; i < ranking.length() - 1; i++) {
      int j = i + random.nextInt(ranking.length() - i);
      ranking.swap(i, j);
    }
  }


}
