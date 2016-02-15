package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class Sample<PS extends PreferenceSet> extends ArrayList<PW<PS>> {

  protected final ItemSet itemSet;

  public Sample(ItemSet items) {
    this.itemSet = items;
  }

  public Sample(Sample<PS> sample) {
    this(sample.itemSet);
    addAll(sample);
  }


  public ItemSet getItemSet() {
    return itemSet;
  }

  /** @return Sum of weights of all appearances of the PreferenceSet.
   */
  public double sumWeights(PS ps) {
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
    this.add(new PW(pref, weight));
  }  

    
  /** Adds weight to the specified preferenceset. It also compresses all occurances of ps into a single one with the total weight 
   * @return true if there were already entries of this ps, false otherwise
   */
  public boolean addWeight(PS ps, double weight) {
    double w = weight;
    boolean were = false;
    Iterator<PW<PS>> it = this.iterator();
    while (it.hasNext()) {
      PW<PS> pw = it.next();
      if (pw.p.equals(ps)) {
        w += pw.w;
        it.remove();
        were = true;
      }
    }
    add(ps, w);
    return were;
  }
  
    
  public PS getPreferenceSet(int index) {
    return this.get(index).p;
  }

  public double getWeight(int index) {
    return this.get(index).w;
  }


  public double getWeight(PS pref) {
    double w = 0;
    for (PW pw: this) w += pw.w;
    return w;
  }

  
  
  /** @return number of ranking instances in the sample (not weighted) */
  public int count(PS ps) {
    int count = 0;
    for (PW pw: this) {
      if (ps.equals(pw.p)) count++;     
    }
    return count;
  }

  public PS[] preferenceSets() {
    PreferenceSet[] rankings = new PreferenceSet[this.size()];
    for (int i = 0; i < rankings.length; i++) {
      rankings[i] = this.get(i).p;      
    }
    return (PS[]) rankings;
  }
  
  public double[] weights() {
    double[] weights = new double[this.size()];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = this.getWeight(i);      
    }
    return weights;
  }
  

  public void addAll(Sample<PS> sample) {
    for (PW pw: sample) {
      this.add(pw.clone());
    }
  }
  
  public void addAll(RankingSample sample, double weight) {
    for (PW pw: sample) {
      this.add(new PW(pw.p.clone(), pw.w * weight));
    }
  }  
  
  public Sample<DensePreferenceSet> transitiveClosure() {
    Sample sample = new Sample(itemSet);
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
  public static class PW<P extends PreferenceSet> implements Cloneable {
    
      public final P p;
      public final double w;

      public PW(P p, double w) {
        this.p = p;
        this.w = w;
      }

      public PW<P> clone() {
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
}
