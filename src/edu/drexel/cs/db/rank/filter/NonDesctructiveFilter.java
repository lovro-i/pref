package edu.drexel.cs.db.rank.filter;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.security.SecureRandom;
import java.util.Random;


public class NonDesctructiveFilter {

  private static Random random = new SecureRandom();


  /** Remove items from the ranking with probability p for removing each one. Non-destructive, returns a new Ranking. */
  public static PreferenceSet removeItems(PreferenceSet pref, double p) {
    PreferenceSet result = pref.clone();
    Filter.removeItems(result, p);
    return result;
  }
  
  /** Remove items from all rankings with probability p for removing each item. Non-destructive, returns a new sample with new rankings. */
  public static Sample<? extends PreferenceSet> removeItems(Sample<? extends PreferenceSet> sample, double p) {
    Sample result = new Sample(sample.getItemSet());
    for (PW pw: sample) {
      PreferenceSet pref = NonDesctructiveFilter.removeItems(pw.p, p);
      result.add(pref, pw.w);
    }
    return result;
  }
  
  /** Remove items from all rankings with probability p for removing each item. Non-destructive, returns a new sample with new rankings. */
  public static RankingSample removeItems(RankingSample sample, double p) {
    RankingSample result = new RankingSample(sample.getItemSet());
    for (PW pw: sample) {
      PreferenceSet pref = NonDesctructiveFilter.removeItems(pw.p, p);
      result.add((Ranking) pref, pw.w);
    }
    return result;
  }
  
  /** Replaces a ranking with a uniformly random one with probability p. Non-destructive, returns a new sample with new rankings. */
  public static RankingSample noise(RankingSample sample, double p) {
    RankingSample result = new RankingSample(sample.getItemSet());
    for (PW pw: sample) {
      Ranking r = new Ranking((Ranking) pw.p);
      double flip = random.nextDouble();
      if (flip < p) r.randomize();
      result.add(r, pw.w);
    }
    return result;
  }
  
  /** Uniformly randomizes the items of the ranking. Non=destructive, returns a new ranking. */
  public static Ranking randomize(Ranking ranking) {
    Ranking r = new Ranking(ranking);
    for (int i = 0; i < r.length() - 1; i++) {
      int j = i + random.nextInt(r.length() - i);
      r.swap(i, j);
    }
    return r;
  }
  
}
