package com.rankst.comb;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import java.security.SecureRandom;
import java.util.Random;

/** For removing elements from rankings and samples */
public class Comb {
  
  private static Random random = new SecureRandom();

  /** Remove elements from ranking with probability p for removing each one */
  public static void comb(Ranking ranking, double p) {
    for (int i = ranking.size()-1; i >= 0; i--) {
      double flip = random.nextDouble();
      if (flip < p) ranking.remove(i);
    }
  }
  
  public static void comb(Sample sample, double p) {
    for (Ranking r: sample) {
      Comb.comb(r, p);
    }
  }
  
  
}
