package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.preference.PreferenceSample;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/** Sample of rankings. Can be weighted if rankings are added through add(Ranking ranking, double weight)
 * 
 */
public class Sample extends ArrayList<RW> {

  private final ItemSet itemSet;
  
  
  public Sample(ItemSet itemSet) {    
    this.itemSet = itemSet;
  }
  
  public Sample(Sample sample) {
    this.itemSet = sample.itemSet;
    addAll(sample);
  }
  

  public ItemSet getItemSet() {
    return itemSet;
  }
  
  public boolean add(Ranking ranking) {
    return this.add(ranking, 1d);
  }
  
  
  public boolean add(Ranking ranking, double weight) {
    return this.add(new RW(ranking, weight));
  }
  
  public boolean addWeight(Ranking ranking, double weight) {
    for (int i = 0; i < size(); i++) {
      RW rw = this.get(i);
      if (rw.r.equals(ranking)) {
        this.set(i, new RW(ranking, rw.w + weight));
        return true;
      }
    }
    
    add(ranking, weight);
    return false;
  }
  
  public void addAll(Sample sample) {
    for (RW rw: sample) {
      this.add(new Ranking(rw.r), rw.w);
    }
  }
  
  public void addAll(Sample sample, double weight) {
    for (RW rw: sample) {
      this.add(new Ranking(rw.r), weight * rw.w);
    }
  }
  
  public double getWeight(int index) {
    return this.get(index).w;
  }
  
  public Ranking[] rankings() {
    Ranking[] rankings = new Ranking[this.size()];
    for (int i = 0; i < rankings.length; i++) {
      rankings[i] = this.get(i).r;      
    }
    return rankings;
  }
  
  public double[] weights() {
    double[] weights = new double[this.size()];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = this.getWeight(i);      
    }
    return weights;
  }
  
  /** @return Sum of weights of all appearances of the ranking. Returns the number of occurrences of the ranking if the sample is not weighted
   */
  public double getWeight(Ranking ranking) {
    double w = 0d;
    for (int index = 0; index < size(); index++) {
      RW rw = this.get(index);
      if (ranking.equals(rw.r)) w += rw.w;
    }
    return w;
  }
  
  /** @return number of ranking instances in the sample (not weighted) */
  public int count(Ranking ranking) {
    int count = 0;
    for (RW rw: this) {
      if (ranking.equals(rw.r)) count++;     
    }
    return count;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this.size(); i++) {
      sb.append(this.get(i));
      sb.append("\n");      
    }
    sb.append("=== ").append(this.itemSet.size()).append(" items, ").append(this.size()).append(" rankings ===");
    return sb.toString();
  }
  
  
  public double sumWeights() {
    double sum = 0;
    for (RW rw: this) sum += rw.w;
    return sum;
  }

  /** Creates a sample of cartesian products; the first half of each ranking is from this sample, the second if from Sample s */
  public Sample multiply(Sample s) {
    Sample sample = new Sample(this.itemSet);
    for (RW rw1: this) {
      for (RW rw2: s) {
        Ranking r = new Ranking(rw1.r);
        r.add(rw2.r);
        sample.add(r, rw1.w * rw2.w);
      }
    }
    return sample;
  }
  
  public PreferenceSample transitiveClosure() {
    PreferenceSample sample = new PreferenceSample(itemSet);
    for (RW rw: this) {
      sample.add(rw.r.transitiveClosure(), rw.w);
    }
    return sample;
  }

  /** Class that represents Ranking - Weight pair */
  public static class RW {
    
    public final Ranking r;
    public final double w;
    
    private RW(Ranking r, double w) {
      this.r = r;
      this.w = w;
    }
    
    @Override
    public String toString() {
      if (w == 1d) return r.toString();
      return r + " (" + w + ")";
    }
    
  }
  
  
  public void save(PrintWriter out) {
    out.println(this.itemSet.size());
    for (int i = 0; i < this.size(); i++) {
      RW rw = this.get(i);
      out.print(rw.r);
      out.print("\t");
      out.print(rw.w);
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
