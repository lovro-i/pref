package edu.drexel.cs.db.rank.filter;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.incomplete.MissingProbabilities;
import edu.drexel.cs.db.rank.util.Logger;
import java.security.SecureRandom;
import java.util.Random;

/** For removing items from rankings and samples */
public class Filter {
  
  private static Random random = new SecureRandom();

  /** Remove items from the ranking with probability p for removing each one. Destructive on ranking, changes the actual ranking */
  public static void remove(Ranking ranking, double p) {
    for (int i = ranking.size()-1; i >= 0; i--) {
      double flip = random.nextDouble();
      if (flip < p) ranking.remove(i);
    }
  }
  
  /** Remove items from the ranking with probabilities specified in Missing. Destructive on ranking, changes the actual ranking */
  public static void remove(Ranking ranking, MissingProbabilities m) {
    m.remove(ranking);
  }
  
  /** Remove items from all rankings with probability p for removing each one. Destructive, changes the actual sample and its rankings */
  public static void remove(RankingSample sample, double p) {
    for (PW<Ranking> pw: sample) {
      Filter.remove(pw.p, p);
    }
  }
  
  
  /** Remove items from all rankings with probabilities specified in Missing. Destructive, changes the actual sample and its rankings */
  public static void remove(RankingSample sample, MissingProbabilities m) {
    m.remove(sample);
  }
  
  
  /** Leave only top K items in each ranking */
  public static void top(RankingSample sample, int k) {
    for (Ranking r: sample.rankings()) {
      while (r.size() > k) r.remove(r.size() - 1);
    }
  }
  
  /** Leave between min and max (both inclusive) items in the ranking. Uniform distribution */
  public static void top(RankingSample sample, int min, int max) {
    for (Ranking r: sample.rankings()) {
      int k = min + random.nextInt(max - min + 1);
      while (r.size() > k) r.remove(r.size() - 1);
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
    for (int i = 0; i < ranking.size() - 1; i++) {
      int j = i + random.nextInt(ranking.size() - i);
      ranking.swap(i, j);
    }
  }

  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    
    RankingSample sample = MallowsUtils.sample(items.getRandomRanking(), 0.3, 100);
    System.out.println(sample);
    
    RankingSample noisy = new RankingSample(sample);
    noise(noisy, 0.2);
    System.out.println(noisy);
    
    int c = 0;
    for (int i = 0; i < sample.size(); i++) {
      if (!sample.get(i).equals(noisy.get(i))) c++;
    }
    System.out.println(c);
  }

}
