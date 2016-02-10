package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.HashMap;
import java.util.Map;


public class ConfidentTriangle extends Triangle {

  protected final Map<Integer, TriangleRow> rows;  
  private final Map<Item, Integer> referenceIndex;
  

  
  public ConfidentTriangle(Ranking reference) {
    super(reference);
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < reference.size(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
    this.referenceIndex = reference.getIndexMap();
  }
  
  public ConfidentTriangle(Ranking reference, Sample sample) {
    this(reference);
//    double per = 0;
    for (RW rw: sample) {
//      double p = 100d * index / sample.size();
//      if (p - per > 10) {
//        per = p;
//        Logger.info(p + "% ");
//      }
      this.add(rw.r, rw.w);
    }
    
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
  
  /** Adds ranking to the triangle with specified weight. Returns true if added, false otherwise */
  public boolean add(Ranking ranking, double weight) {
    for (int i = 0; i < reference.size(); i++) {
      TriangleRow row = rows.get(i); // Triangle row to be updated
      Item e = reference.get(i);
      
      UpTo upto = new UpTo(ranking, i, referenceIndex);
      int pos = upto.position;
      
      if (pos == -1) return true;                            
      row.inc(pos, weight);
    }
    return true;
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
    Filter.remove(r1, 0.2);
    triangle.add(r1, 1);
    System.out.println(r1);
    System.out.println(triangle);
    
  }
    
}
