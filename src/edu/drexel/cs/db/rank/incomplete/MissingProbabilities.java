package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import java.util.Random;

/** Class that stores information for each item in the ItemSet about its missing probability */
public class MissingProbabilities {

  private static Random random = new Random();
  
  private ItemSet items;
  private double[] miss;
  
  
  public MissingProbabilities(ItemSet items, double[] miss) {
    this.items = items;
    this.miss = new double[items.size()];
    for (int i = 0; i < this.miss.length; i++) {
      this.miss[i] = miss[i];
    }
  }
  
  
  /** Uniform missing rate for all items */
  public MissingProbabilities(ItemSet items, double p) {
    this.items = items;
    this.miss = new double[items.size()];
    for (int i = 0; i < miss.length; i++) {
      miss[i] = p;      
    }
  }
  
  /** create the missing statistics from the sample */
  public MissingProbabilities(RankingSample sample) {
    this.items = sample.getItemSet();
    this.miss = new double[items.size()];
    
    int[] counts = new int[items.size()];
    for (PW<Ranking> pw: sample) {
      for (Item e: pw.p.getItems()) {
        counts[e.getId()] += pw.w;
      }
    }
    
    
    int ss = sample.size();
    for (int i = 0; i < counts.length; i++) {
      miss[i] = 1d * (ss - counts[i]) / ss;
    }    
  }
  

  
  /** Remove items randomly from the ranking with specified probabilities */
  public void remove(Ranking ranking) {
    for (int i = ranking.size()-1; i >= 0; i--) {
      Item e = ranking.get(i);
      double flip = random.nextDouble();
      if (flip < get(e)) ranking.remove(i);
    }
  }
    
  
  /** Remove preferences randomly from this PreferenceSet 
   * Each preference should be removed with probability that either item1 or item2 is removed 
   */
  public void remove(PreferenceSet prefs) {
    // @todo Haoyue
  }
  
  
  /** Remove preferences randomly from the sample with specified probabilities
   * @param sample */
  public void remove(Sample<? extends PreferenceSet> sample) {
    for (PreferenceSet ps: sample.preferenceSets()) {
      remove(ps);
    }
  }
  
  
  public double get(Item e) {
    return miss[e.getId()];
  }
  
  
  public double get(int id) {
    return miss[id];
  }
  
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < miss.length; i++) {
      Item e = items.get(i);
      sb.append(e).append(": ").append(miss[i]).append("\n");
    }
    return sb.toString();
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    RankingSample sample = MallowsUtils.sample(items.getRandomRanking(), 0.3, 2000);
    Filter.remove(sample, 0.3);
    
    MissingProbabilities m = new MissingProbabilities(sample);
    System.out.println(m);
  }
  
}
