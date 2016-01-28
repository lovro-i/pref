package edu.drexel.cs.db.rank.top;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.triangle.Triangle;
import edu.drexel.cs.db.rank.triangle.TriangleRow;
import edu.drexel.cs.db.rank.triangle.UpTo;
import java.util.HashMap;
import java.util.Map;


public class TopSampleTriangle extends Triangle {

  protected final Map<Integer, TriangleRow> rows;
  
  public TopSampleTriangle(Ranking reference) {
    super(reference);
    this.buildReferenceIndexMap();
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < reference.size(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
  }
  
  public TopSampleTriangle(Ranking reference, Sample sample) {
    this(reference);
    for (int index = 0; index < sample.size(); index++) {
      Ranking ranking = sample.get(index);
      double weight = sample.getWeight(index);
      this.add(ranking, weight);
    }
    
  }
  
  public TriangleRow getRow(int i) {
    return rows.get(i);
  }
  
  public TriangleRow getRow(Item e) {
    int index = reference.indexOf(e);
    return rows.get(index);
  }
  
  /** Get random position for the item based on added rankings */
  @Override
  public int randomPosition(int e) {
    return rows.get(e).random();
  }
  
  /** Adds ranking to the triangle with specified weight. Returns true if added, false otherwise */
  public boolean add(Ranking ranking, double weight) {
    
    TopExpands expands = new TopExpands();
    expands.nullify();
    
    for (int i = 0; i < reference.size(); i++) {
      TriangleRow row = rows.get(i); // Triangle row to be updated
      Item e = reference.get(i);
      
      UpTo upto = new UpTo(ranking, i, referenceIndex);
      int pos = upto.position;
      
      if (pos == -1) { // This one is missing. Distribute evenly its probability
        expands = expands.insertMissing();
        continue;
      }
                  
      expands = expands.insert(e, upto.previous); //new 
      
      double[] displacements = expands.getDistribution(e);
      for (int j = 0; j < displacements.length; j++) {
        row.inc(j, displacements[j] * weight);
      }
      
    }
    return true;
  }


  
  private Map<Item, Integer> referenceIndex = new HashMap<Item, Integer>();
  
  private void buildReferenceIndexMap() {
    referenceIndex.clear();
    for (int i = 0; i < reference.size(); i++) {
      Item e = reference.get(i);
      referenceIndex.put(e, i);
    }    
  }
  
  /** Return the ranking containing only the items up to (and including) max */
  private Ranking upTo(Ranking ranking, int max) {
    Ranking r = new Ranking(ranking.getItemSet());
    for (int i=0; i<ranking.size(); i++) {
      Item e = ranking.get(i);
      int index = referenceIndex.get(e);
      if (index <= max) r.add(e);
    }
    return r;
  }
 
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < getItemSet().size(); i++) {
      sb.append(rows.get(i)).append("\n");
    }  
    return sb.toString();
  }
  
}


