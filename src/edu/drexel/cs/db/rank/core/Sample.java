package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class Sample<PS extends PreferenceSet> extends ArrayList<PW<PS>> {

  protected final ItemSet itemSet;
  private static final long serialVersionUID = 8089688392277174718L;


  public Sample(ItemSet items) {
    this.itemSet = items;
  }

  public Sample(Sample<? extends PS> sample) {
    this(sample.itemSet);
    addAll(sample);
  }
    
  public ItemSet getItemSet() {
    return itemSet;
  }

  /** @return Sum of weights of all appearances of the PreferenceSet.
   */
  public double sumWeights(PreferenceSet ps) {
    double w = 0;
    for (PW pw: this) {
      if (ps.equals(pw.p)) w += pw.w;
    }
    return w;
  }
  
  public double sumWeights() {
    double s = 0;
    for (PW pw: this) s += pw.w;
    return s;
  }

  public void add(PS pref) {
    this.add(pref, 1);
  }

  public void add(PS pref, double weight) {
    this.add(new PW<PS>(pref, weight));
  }  
  
  public void set(int index, PS pref, double weight) {
    this.set(index, new PW<PS>(pref, weight));
  }

    
  /** Adds weight to the specified PreferenceSet. It also compresses all occurrences of ps into a single one with the total weight 
   * @return true if there were already entries of this ps, false otherwise
   */
  public boolean addWeight(PS ps, double weight) {
    double w = weight;
    boolean were = false;
    Iterator<PW<PS>> it = this.iterator();
    while (it.hasNext()) {
      PW pw = it.next();
      if (pw.p.equals(ps)) {
        w += pw.w;
        it.remove();
        were = true;
      }
    }
    add(ps, w);
    return were;
  }
  
    
  public PreferenceSet getPreferenceSet(int index) {
    return this.get(index).p;
  }

  public double getWeight(int index) {
    return this.get(index).w;
  }


  public double getWeight(PreferenceSet pref) {
    double w = 0;
    for (PW pw: this) w += pw.w;
    return w;
  }

  
  
  /** @return number of ranking instances in the sample (not weighted) */
  public int count(PreferenceSet ps) {
    int count = 0;
    for (PW pw: this) {
      if (ps.equals(pw.p)) count++;     
    }
    return count;
  }

  /** @return array of all PreferenceSets */
  public PreferenceSet[] preferenceSets() {
    PreferenceSet[] rankings = new PreferenceSet[this.size()];
    for (int i = 0; i < rankings.length; i++) {
      rankings[i] = this.get(i).p;      
    }
    return (PreferenceSet[]) rankings;
  }
  
  /** @return array of all weights */
  public double[] weights() {
    double[] weights = new double[this.size()];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = this.getWeight(i);      
    }
    return weights;
  }
  
  
  public void addAll(Sample<? extends PS> sample) {
    for (PW pw: sample) {
      this.add(pw.clone());
    }
  }
  
  public void addAll(Sample<? extends PS> sample, double weight) {
    for (PW pw: sample) {
      this.add(new PW(pw.p.clone(), pw.w * weight));
    }
  }  
  
  public Sample<PreferenceSet> transitiveClosure() {
    Sample<PreferenceSet> sample = new Sample<PreferenceSet>(itemSet);
    for (PW pw: this) {
      sample.add(pw.p.transitiveClosure(), pw.w);
    }
    return sample;
  }
    
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this.size(); i++) {
      sb.append(this.get(i));
      sb.append("\n");      
    }
    sb.append("=== ").append(this.itemSet.size()).append(" items, ").append(this.size()).append(" preference sets ===");
    return sb.toString();
  }

  
  /** PreferenceSet - Weight pair */
  public static class PW<PS extends PreferenceSet> implements Cloneable, Serializable {

    public final PS p;
    public final double w;

    public PW(PS p, double w) {
      this.p = p;
      this.w = w;
    }

    public PW clone() {
      return new PW(p.clone(), w);
    }

    public String toString() {
      if (w == 1) return p.toString();
      else return p.toString() + " (" + w + ")";
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 89 * hash + Objects.hashCode(this.p);
      hash = 89 * hash + (int) (Double.doubleToLongBits(this.w) ^ (Double.doubleToLongBits(this.w) >>> 32));
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final PW other = (PW) obj;
      return other.p.equals(p) && w == other.w;
    }

  }
  
  
  public void save(OutputStream out) throws IOException {
    ObjectOutputStream os = new ObjectOutputStream(out);
    os.writeObject(this);
  }
  
  public void save(File file) throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    save(out);
    out.close();
  } 
  
  
  public static Sample<PreferenceSet> load(InputStream in) throws IOException, ClassNotFoundException {
    ObjectInputStream is = new ObjectInputStream(in);
    return (Sample<PreferenceSet>) is.readObject();
  }
 
  public static Sample<PreferenceSet> load(File file) throws IOException, ClassNotFoundException {
    return load(new FileInputStream(file));
  }
  
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    ItemSet items = new ItemSet(10);
    RankingSample rankings = MallowsUtils.sample(items.getRandomRanking(), 0.2, 5);
    rankings.save(new File("rankings.ser"));
    
    Sample<PreferenceSet> tc = rankings.transitiveClosure();
    System.out.println(tc);
    
    String filename = "c:/temp/sample.ser";
    tc.save(new File(filename));
    
    Sample<PreferenceSet> loaded = Sample.load(new File(filename));
    System.out.println(loaded);

  }
  
}
