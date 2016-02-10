package edu.drexel.cs.db.rank.distance;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class KendallTauCountOld {

  private static Map<Entry, Double> distances = new HashMap<Entry, Double>();

  /** Get the number of rankings of length n at the distance d */
  public static synchronized double getCount(int n, int d) {
    if (d < 0) return 0;
    if (n < 1) return 0;
    if (d == 0) return 1;
    if (d == 1) return n-1;
    int max = n * (n-1) / 2;
    if (d > max) return 0;
    
    Entry entry = new Entry(n, d);
    if (distances.containsKey(entry)) {
      return distances.get(entry);
    }
    else {
      double dist = getCount(n-1, d) + getCount(n, d-1) - getCount(n-1, d-n);
      distances.put(entry, dist);
      return dist;
    }
  }
  
  
  private static class Entry {
    private int n, d;
    
    private Entry(int n, int d) {
      this.n = n;
      this.d = d;
    }

    @Override
    public boolean equals(Object obj) {
      Entry e = (Entry) obj;
      return this.n == e.n && this.d == e.d;
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 29 * hash + this.n;
      hash = 29 * hash + this.d;
      return hash;
    }
    
  }
  
}
