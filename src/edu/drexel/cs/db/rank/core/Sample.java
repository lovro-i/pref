package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/** Sample of rankings. Can be weighted if rankings are added through add(Ranking ranking, double weight)
 * 
 */
public class Sample extends ArrayList<Ranking> {

  private final ItemSet itemSet;
  
  private ArrayList<Double> weights;
  
  public Sample(ItemSet itemSet) {    
    this.itemSet = itemSet;
  }
  
  public Sample(Sample sample) {
    this.itemSet = sample.itemSet;
    if (sample.weights != null) initWeights();
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i);
      Ranking rc = new Ranking(r);
      if (sample.weights == null) this.add(rc);
      else this.add(rc, sample.getWeight(i));
    }
  }
  

  public ItemSet getItemSet() {
    return itemSet;
  }
  
  @Override
  public boolean add(Ranking ranking) {
    if (weights != null) weights.add(1d);    
    return super.add(ranking);
  }
  
  /** Initializes weights array if needed. Returns true if initalized, false if it was previously initialized */
  private boolean initWeights() {
    if (weights != null) return false;
    weights = new ArrayList<Double>();
    for (int i = 0; i < this.size(); i++) weights.add(1d);
    return true;
  }
  
  public boolean add(Ranking ranking, double weight) {
    initWeights();
    weights.add(weight);
    return super.add(ranking);
  }
  
  public void addAll(Sample sample) {
    if (!this.itemSet.equals(sample.itemSet)) throw new IllegalArgumentException("ItemSets do not match");
    
    if (this.weights == null && sample.weights == null) {
      super.addAll(sample);
      return;
    }
    
    for (RW rw: sample.enumerate()) {
      this.add(rw.r, rw.w);
    }
  }
  
  public void addAll(Sample sample, double weight) {
    if (!this.itemSet.equals(sample.itemSet)) {
      throw new IllegalArgumentException("ItemSets do not match");
    }
    
    for (RW rw: sample.enumerate()) {
      this.add(rw.r, rw.w * weight);
    }
  }
  
  public double getWeight(int index) {
    if (weights == null) return 1d;
    return weights.get(index);
  }
  
  public ArrayList<Double> getWeights() {
    initWeights();
    return this.weights;
  }
  
  /** @return Sum of weights of all appearances of the ranking. Returns the number of occurrences of the ranking if the sample is not weighted
   */
  public double getWeight(Ranking ranking) {
    if (weights == null) return count(ranking);
    double w = 0d;
    for (int index = 0; index < size(); index++) {
      Ranking r = this.get(index);
      if (ranking.equals(r)) w += weights.get(index);     
    }
    return w;
  }
  
  /** @return number of specified rankings in the sample */
  public int count(Ranking ranking) {
    int count = 0;
    for (Ranking r: this) {
      if (ranking.equals(r)) count++;     
    }
    return count;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this.size(); i++) {
      sb.append(this.get(i));
      if (weights != null)
        sb.append(" (").append(weights.get(i)).append(")");
      sb.append("\n");      
    }
    sb.append("=== ").append(this.itemSet.size()).append(" items, ").append(this.size()).append(" rankings ===");
    return sb.toString();
  }
  
  
  public List<RW> enumerate() {
    List<RW> list = new ArrayList<RW>();
    for (int i = 0; i < size(); i++) {
      Ranking r = this.get(i);
      double w = this.getWeight(i);
      list.add(new RW(r, w));
    }
    return list;
  }

  public double sumWeights() {
    if (weights == null) return size();
    double sum = 0;
    for (double w: weights) sum += w;
    return sum;
  }

  /** Creates a sample of cartesian products; the first half of each ranking is from this sample, the second if from Sample s */
  public Sample multiply(Sample s) {
    Sample sample = new Sample(this.itemSet);
    for (Ranking r1: this) {
      double w1 = this.getWeight(r1);
      for (Ranking r2: s) {
        Ranking r = new Ranking(r1);
        r.add(r2);
        sample.add(r, w1 * s.getWeight(r2));
      }
    }
    return sample;
  }

  public void setWeights(double d) {
    if (weights == null) weights = new ArrayList<Double>();
    else weights.clear();
    for (int i = 0; i < size(); i++) {
      weights.add(d);
    }
  }
 
  
  /** Class that represents Ranking - Weight pair */
  public static class RW {
    
    public final Ranking r;
    public final double w;
    
    private RW(Ranking r, double w) {
      this.r = r;
      this.w = w;
    }
    
  }
  
  
  public void save(PrintWriter out) {
    out.println(this.itemSet.size());
    for (int i = 0; i < this.size(); i++) {
      Ranking r = this.get(i);
      out.print(r);
      if (this.weights != null) {
        out.print("\t");
        out.print(this.weights.get(i));
      }
      out.println();
    }    
  }
  
  public void save(File file) throws IOException {
    PrintWriter out = FileUtils.write(file);
    save(out);
    out.close();
  }


  public static void main(String[] args) throws IOException {
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File file = new File(folder, "first.sample");
    
    ItemSet items = new ItemSet(15);
    Ranking center = items.getRandomRanking();
    Sample sample = MallowsUtils.sample(center, 0.5, 200);
    sample.save(file);
    System.out.println(sample);   
    
  }

}
