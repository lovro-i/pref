package edu.drexel.cs.db.db4pref.top;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample.PW;
import edu.drexel.cs.db.db4pref.util.MathUtils;

/** Distribution of ranking lengths */
public class Tops {

  private final ItemSet items;
  private double[] lengths;
  
  public Tops(RankingSample sample) {
    this.items = sample.getItemSet();
    lengths = new double[items.size() + 1];
    double sum = 0;
    for (PW<Ranking> pw: sample) {
      int i = pw.p.length();
      lengths[i] += pw.w;
      sum += pw.w;
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
    while (ranking.length() > k) ranking.remove(ranking.length() - 1);
  }
    
  /** Remove items randomly from the sample with specified probabilities */
  public void remove(RankingSample sample) {
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
