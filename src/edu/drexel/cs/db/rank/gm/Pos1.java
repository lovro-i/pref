package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.model.MallowsModel;
import java.util.HashMap;
import java.util.Map;

/** Calculates probabilities of POS1 factors using dynamic recursive algorithm (caches previously calculated values) */
public class Pos1 {

  private MallowsModel model;
  private Map<Entry, Double> cache = new HashMap<Entry, Double>();

  public Pos1(MallowsModel model) {
    this.model = model;
  }

  public void clear() {
    cache.clear();
  }
  
  /**
   * Probability of Xij = vij given Xik = vik
   */
  public double getProbability(Item item, int j, int vj, int k, int vk) {
    int i = model.getCenter().indexOf(item);
    if (j < i) return 0;
    if (k < i) return 0;
    if (vj < vk) return 0;
    if (j == k) {
      if (vj == vk) return 1;
      else return 0;
    }
    
    Entry entry = new Entry(item, j, vj, k, vk);
    if (cache.containsKey(entry)) return cache.get(entry);
    
    double p = calcProbability(item, j, vj, k, vk);
    cache.put(entry, p);
    return p;
  }
  
  private double calcProbability(Item item, int j, int vj, int k, int vk) {
    double p1 = 0;
    for (int q = 0; q <= vj - 1; q++) {
      p1 += w(j, q);
    }
    
    double p2 = getProbability(item, j-1, vj-1, k, vk);
    
    double p3 = 0;
    for (int q = vj + 1; q <= j; q++) {
      p3 += w(j, q);
    }
    
    double p4 = getProbability(item, j-1, vj, k, vk);
    
    return p1 * p2 + p3 * p4;
  }

  /** Insertion probability
   * @param i item
   * @param k position
   * @return Xi^i = k
   */
  private double w(int i, int k) {
    if (i == 0) return 1d;
    double phi = model.getPhi();
    double p = Math.pow(phi, i - k) * (1 - phi) / (1 - Math.pow(phi, i + 1));
    return p;
  }
  
  private static class Entry {

    private int i;
    private int j;
    private int vj;
    private int k;
    private int vk;

    private Entry(Item item, int j, int vj, int k, int vk) {
      this.i = item.id;
      this.j = j;
      this.vj = vj;
      this.k = k;
      this.vk = vk;
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 89 * hash + this.i;
      hash = 89 * hash + this.j;
      hash = 89 * hash + this.vj;
      hash = 89 * hash + this.k;
      hash = 89 * hash + this.vk;
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final Entry other = (Entry) obj;
      if (this.i != other.i) return false;
      if (this.j != other.j) return false;
      if (this.vj != other.vj) return false;
      if (this.k != other.k) return false;
      if (this.vk != other.vk) return false;
      return true;
    }

    

  }
}
