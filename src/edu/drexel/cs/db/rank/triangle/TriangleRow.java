package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.util.MathUtils;


public class TriangleRow {
  
  private final double[] counts;
  private double sum;
  private boolean empty;
  
  public TriangleRow(int n) {
    counts = new double[n+1];
    sum = 0;
    empty = true;
  }
  
  public TriangleRow(double[] p) {
    counts = p;
    sum = MathUtils.sum(p);
    empty = false;
  }
  
  public void inc(int pos, double add) {
    counts[pos] += add;
    sum += add;
    empty = false;
  }
  
  public void inc(int pos) {
    inc(pos, 1);
  }
  
  public int size() {
    return counts.length;  
  }
  
  public boolean isEmpty() {
    return empty;
  }
  
  public double getProbability(int index) {
    return counts[index] / sum;
  }
  
  /** Returns the probability that the item is between index start (inclusive) and end (exclusive) */
  public double getProbability(int start, int end) {
    if (empty) return 1d * (end - start) / counts.length;    
    
    double s = 0;
    for (int i = start; i < end; i++) {
      s += counts[i];      
    }
    return s / sum;
  }
  
  double[] getCounts() {
    return counts;
  }
  
  public double getCount(int index) {
    return counts[index];
  }
  
  public double getSum() {
    return sum;
  }
  
  /** Sum of counts between low (inclusive) and high (exclusive) */
  public double getCount(int low, int high) {
    double s = 0;
    for (int i = low; i < high; i++) {
      s += counts[i];     
    }
    return s;
  }
  
  /** Returns random position with calculated probabilities */
  public int getRandomPosition() {
    if (empty) return SampleTriangle.random.nextInt(counts.length);
    
    double p = SampleTriangle.random.nextDouble() * sum;
    double s = 0;
    for (int i = 0; i < counts.length; i++) {
      s += counts[i];
      if (s > p) return i;
    }
    return counts.length - 1;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Item ").append(counts.length-1).append(":");
    for (int j=0; j<counts.length; j++) sb.append(" ").append(counts[j]);
    return sb.toString();
  }
}