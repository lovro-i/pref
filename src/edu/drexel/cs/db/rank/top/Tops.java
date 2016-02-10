package edu.drexel.cs.db.rank.top;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.util.MathUtils;

/** Distribution of ranking length */
public class Tops {

  private final ItemSet items;
  private double[] lengths;
  
  public Tops(Sample sample) {
    this.items = sample.getItemSet();
    lengths = new double[items.size() + 1];
    double sum = 0;
    for (RW rw: sample) {
      int i = rw.r.size();
      lengths[i] += rw.w;
      sum += rw.w;
    }
    
    for (int i = 0; i < lengths.length; i++) {
      lengths[i] = lengths[i] / sum;      
    }
  }
  
  
  public int pickLength() {
    double p = MathUtils.RANDOM.nextDouble();
    double s = 0;
    for (int i = 0; i < lengths.length - 1; i++) {
      s += lengths[i];
      if (s > p) return i;
    }
    return lengths.length;
  }
  
  /** Remove items randomly from the ranking with specified probabilities */
  public void remove(Ranking ranking) {
    int k = pickLength();
    while (ranking.size() > k) ranking.remove(ranking.size() - 1);
  }
    
  /** Remove items randomly from the sample with specified probabilities */
  public void remove(Sample sample) {
    for (Ranking r: sample.rankings()) {
      remove(r);
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lengths.length; i++) {
      sb.append(i).append(": ").append(lengths[i]).append('\t');
    }
    return sb.toString();
  }
  
}
