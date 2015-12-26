package com.rankst.entity;

import java.util.ArrayList;
import java.util.List;

/** Sample of rankings. Can be weighted if rankings are added through add(Ranking ranking, double weight)
 * 
 */
public class Sample extends ArrayList<Ranking> {

  private final ElementSet elements;
  
  private ArrayList<Double> weights;
  
  public Sample(ElementSet elements) {    
    this.elements = elements;
  }
  
  public Sample(Sample sample) {
    this.elements = sample.elements;
    if (sample.weights != null) initWeights();
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i);
      Ranking rc = new Ranking(r);
      if (sample.weights == null) this.add(rc);
      else this.add(rc, sample.getWeight(i));
    }
  }

  public ElementSet getElements() {
    return elements;
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
    sb.append("=== ").append(this.size()).append(" samples ===");
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
 
  
  /** Class that represents Ranking - Weight pair */
  public static class RW {
    
    public final Ranking r;
    public final double w;
    
    private RW(Ranking r, double w) {
      this.r = r;
      this.w = w;
    }
    
  }
  
}
