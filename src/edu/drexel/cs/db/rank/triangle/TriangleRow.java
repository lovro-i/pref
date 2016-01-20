package edu.drexel.cs.db.rank.triangle;


class TriangleRow {
  
  private final double[] counts;
  private double sum;
  private boolean empty;
  
  public TriangleRow(int n) {
    counts = new double[n+1];
    sum = 0;
    empty = true;
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
  
  double getSum() {
    return sum;
  }
  
  /** @return random weighted position [0..n) */
  public int random() {
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