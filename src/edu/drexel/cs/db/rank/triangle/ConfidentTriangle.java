package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** RIM triangle that contains only certain information from each preference. Preference is discarded as soon as a missing item is reached. */
public class ConfidentTriangle extends Triangle {

  protected final Map<Integer, TriangleRow> rows;  

  
  public ConfidentTriangle(Ranking reference) {
    super(reference);
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < reference.length(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
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
  
  public void add(Sample<PreferenceSet> sample) {
    if (sample == null) return;
    for (PW pw: sample) {
      this.add(pw.p, pw.w);
    }
  }
  
  /** Adds ranking to the triangle with specified weight. Returns true if added, false otherwise */
  public boolean add(PreferenceSet pref, double weight) {
    Set<Item> items = new HashSet<Item>();
    for (int i = 0; i < reference.length(); i++) {
      TriangleRow row = rows.get(i); // Triangle row to be updated
      Item e = reference.get(i);
      items.add(e);
      
      Ranking r = pref.toRanking(items);
      int pos = r.indexOf(e);      
      if (pos == -1) return true;                            
      row.inc(pos, weight);
    }
    return true;
  }
  
  public void add(int row, int pos, double weight) {
    rows.get(row).inc(pos, weight);
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
    ItemSet items = new ItemSet(6);
    Ranking ref = items.getReferenceRanking();
    
    ConfidentTriangle triangle = new ConfidentTriangle(ref);
    
    Ranking r1 = items.getRandomRanking();
    Filter.removeItems(r1, 0.2);
    triangle.add(r1, 1);
    System.out.println(r1);
    System.out.println(triangle);
    
  }
    
}
