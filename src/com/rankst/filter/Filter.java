package com.rankst.filter;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import java.security.SecureRandom;
import java.util.Random;

/** For removing elements from rankings and samples */
public class Filter {
  
  private static Random random = new SecureRandom();

  /** Remove elements from the ranking with probability p for removing each one */
  public static void remove(Ranking ranking, double p) {
    for (int i = ranking.size()-1; i >= 0; i--) {
      double flip = random.nextDouble();
      if (flip < p) ranking.remove(i);
    }
  }
  
  /** Remove elements from all rankings with probability p for removing each one */
  public static void remove(Sample sample, double p) {
    for (Ranking r: sample) {
      Filter.remove(r, p);
    }
  }
  
  /** Swap two adjacent elements in the ranking with probability p (for each two elements) */
  public static void swap(Ranking r, double p) {
    for (int i = 0; i < r.size()-1; i++) {
      double flip = random.nextDouble();
      if (flip < p) r.swap(i, i+1);
    }
  }
  
  /** Swap two adjacent elements all the rankings with probability p (for each two elements) */
  public static void swap(Sample sample, double p) {
    for (Ranking r: sample) {
      Filter.swap(r, p);
    }
  }
  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(10);
    Ranking ranking = elements.getRandomRanking();
    System.out.println(ranking);
    
    swap(ranking, 0.2);
    System.out.println(ranking);
  }
}
