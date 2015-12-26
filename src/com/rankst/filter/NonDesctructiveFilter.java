package com.rankst.filter;

import com.rankst.entity.Element;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.util.MathUtils;
import java.security.SecureRandom;
import java.util.Random;


public class NonDesctructiveFilter {

    private static Random random = new SecureRandom();

  /** Remove elements from the ranking with probability p for removing each one. Non-destructive, returns a new Ranking. */
  public static Ranking remove(Ranking ranking, double p) {
    Ranking result = new Ranking(ranking.getElementSet());
    for (Element e: ranking.getElements()) {
      double flip = random.nextDouble();
      if (flip >= p) result.add(e);
    }
    return result;
  }
  
  /** Remove elements from all rankings with probability p for removing each element. Non-destructive, returns a new sample with new rankings. */
  public static Sample remove(Sample sample, double p) {
    Sample result = new Sample(sample.getElements());
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i);
      Ranking r1 = remove(r, p);
      double w = sample.getWeight(i);
      result.add(r1, w);
    }
    return result;
  }
  
  /** Replaces a ranking with a uniformly random one with probability p. Non-destructive, returns a new sample with new rankings. */
  public static Sample noise(Sample sample, double p) {
    Sample result = new Sample(sample.getElements());
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i);
      double w = sample.getWeight(i);
      double flip = random.nextDouble();
      if (flip < p) r = randomize(r);
      result.add(r, w);
    }
    return result;
  }
  
  /** Uniformly randomizes the elements of the ranking. Non=destructive, returns a new ranking. */
  public static Ranking randomize(Ranking ranking) {
    Ranking r = new Ranking(ranking);
    for (int i = 0; i < r.size() - 1; i++) {
      int j = i + random.nextInt(r.size() - i);
      r.swap(i, j);
    }
    return r;
  }
  
}
