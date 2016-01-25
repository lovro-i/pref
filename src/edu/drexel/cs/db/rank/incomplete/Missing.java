package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import java.util.Random;

/** Class that stores information for each item in the ItemSet about its missing probability */
public class Missing {

  private static Random random = new Random();
  
  private ItemSet items;
  private double[] miss;
  
  
  /** create the missing statistics from the sample */
  public Missing(Sample sample) {
    this.items = sample.getItemSet();
    this.miss = new double[items.size()];
    
    int[] counts = new int[items.size()];
    for (Ranking r: sample) {
      for (Item e: r.getItems()) {
        counts[e.getId()]++;
      }
    }
    
    
    int ss = sample.size();
    for (int i = 0; i < counts.length; i++) {
      miss[i] = 1d * (ss - counts[i]) / ss;
    }    
  }
  
  /** Uniform missing rate for all items */
  public Missing(ItemSet items, double p) {
    this.items = items;
    this.miss = new double[items.size()];
    for (int i = 0; i < miss.length; i++) {
      miss[i] = p;      
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
    
  /** Remove items randomly from the sample with specified probabilities */
  public void remove(Sample sample) {
    for (Ranking r: sample) {
      remove(r);
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
    Sample sample = MallowsUtils.sample(items.getRandomRanking(), 0.3, 2000);
    Filter.remove(sample, 0.3);
    
    Missing m = new Missing(sample);
    System.out.println(m);
  }
  
}
