package edu.drexel.cs.db.db4pref.sampler.triangle;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.Sample.PW;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.sampler.MallowsUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** IPM triangle that contains only certain information from each preference. Preference is discarded as soon as a missing item is encountered. */
public class ConfidentTriangle extends Triangle implements Cloneable {

  protected final Map<Integer, TriangleRow> rows;  

  
  public ConfidentTriangle(Ranking reference) {
    super(reference);
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < reference.length(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
  }
  
  @Override
  public ConfidentTriangle clone() {
    ConfidentTriangle triangle = new ConfidentTriangle(reference);
    for (Integer r: rows.keySet()) {
      TriangleRow src = rows.get(r);
      TriangleRow dst = triangle.rows.get(r);
      dst.clone(src);
    }
    return triangle;
  }
  
  public void clone(ConfidentTriangle triangle) {
    for (Integer r: rows.keySet()) {
      TriangleRow src = triangle.rows.get(r);
      TriangleRow dst = rows.get(r);      
      dst.clone(src);
    }
  }
  
  public ConfidentTriangle(Ranking reference, Sample sample) {
    this(reference);
    add(sample);    
  }
  
  public double get(int item, int pos) {
    return rows.get(item).getProbability(pos);
  }
  
  @Override
  public TriangleRow getRow(int i) {
    return rows.get(i);
  }

  /** Get random position for the item based on added rankings */
  @Override
  public int randomPosition(int e) {
    return rows.get(e).getRandomPosition();
  }
  
  public void add(Sample<? extends PreferenceSet> sample) {
    if (sample == null) return;
    for (PW pw: sample) {
      this.add(pw.p, pw.w);
    }
  }
  
  public void add(Insertions ins, double w) {
    for (int i = 0; i < ins.length; i++) {
      TriangleRow row = rows.get(i);
      int pos = ins.ins[i];
      row.inc(pos, w);
    }
  }
  
  public void sub(Insertions ins, double w) {
    add(ins, -w);
  }
  
  /** Adds ranking to the triangle with specified weight. Returns true if added, false otherwise */
  public void add(PreferenceSet pref, double weight) {
    Set<Item> items = new HashSet<Item>();
    for (int i = 0; i < reference.length(); i++) {
      TriangleRow row = rows.get(i); // Triangle row to be updated
      Item e = reference.get(i);
      items.add(e);
      
      Ranking r = pref.toRanking(items);
      int pos = r.indexOf(e);      
      if (pos == -1) return;                            
      row.inc(pos, weight);
    }
  }
  
  public void add(int row, int pos, double weight) {
    rows.get(row).inc(pos, weight);
  }

  public void add(Triangle triangle, double w) {
    for (Integer i: rows.keySet()) {
      TriangleRow thisRow = this.getRow(i);
      TriangleRow thatRow = triangle.getRow(i);
      thisRow.add(thatRow, w);
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < getItemSet().size(); i++) {
      sb.append(rows.get(i)).append("\n");
    }  
    return sb.toString();
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(50);
    RankingSample sample = MallowsUtils.sample(items.getReferenceRanking(), 0.2, 10000);

    long start = System.currentTimeMillis();
    ConfidentTriangle triangle = new ConfidentTriangle(items.getReferenceRanking(), sample);
    Logger.info("Done id %d ms", (System.currentTimeMillis() - start));
  }
    
}
