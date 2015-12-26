package com.rankst.filter;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.MallowsUtils;
import java.security.SecureRandom;
import java.util.Random;

/** For removing elements from rankings and samples */
public class Filter {
  
  private static Random random = new SecureRandom();

  /** Remove elements from the ranking with probability p for removing each one. Destructive on ranking, changes the actual ranking */
  public static void remove(Ranking ranking, double p) {
    for (int i = ranking.size()-1; i >= 0; i--) {
      double flip = random.nextDouble();
      if (flip < p) ranking.remove(i);
    }
  }
  
  /** Remove elements from all rankings with probability p for removing each one. Destructive, changes the actual sample and its rankings */
  public static void remove(Sample sample, double p) {
    for (Ranking r: sample) {
      Filter.remove(r, p);
    }
  }
  
  /** Replaces a ranking with a uniformly random one with probability p */
  public static void noise(Sample sample, double p) {
    for (Ranking r: sample) {
      double flip = random.nextDouble();
      if (flip < p) randomize(r);
    }
  }
  
  /** Uniformly randomizes the elements of the ranking. Destructive, changes the actual ranking */
  public static void randomize(Ranking ranking) {
    for (int i = 0; i < ranking.size() - 1; i++) {
      int j = i + random.nextInt(ranking.size() - i);
      ranking.swap(i, j);
    }
  }

  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(10);
    
    Sample sample = MallowsUtils.sample(elements.getRandomRanking(), 0.3, 100);
    System.out.println(sample);
    
    Sample noisy = new Sample(sample);
    noise(noisy, 0.2);
    System.out.println(noisy);
    
    int c = 0;
    for (int i = 0; i < sample.size(); i++) {
      if (!sample.get(i).equals(noisy.get(i))) c++;
    }
    System.out.println(c);
  }
}
