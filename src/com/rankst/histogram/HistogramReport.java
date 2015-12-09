package com.rankst.histogram;


public class HistogramReport {
  
  private final Histogram<Integer> histogram;

  public HistogramReport(Histogram<Integer> histogram) {
    this.histogram = histogram;
  }
  
  public int getMax() {
    int max = 0;
    for (Integer i: histogram.map.keySet()) max = Math.max(max, i);
    return max;
  }
  
  public void out() {
    int n = getMax();
    for (int i = 0; i <= n; i++) {
      Double v = histogram.get(i);
      if (v == null) v = 0d;
      System.out.println(String.format("%d\t%d", i, v));
    }
  }
  
}
