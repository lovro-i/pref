package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.HashMap;
import java.util.Map;

/** Fill the triangle item by item first, not by rankings */
public class SampleTriangleByRow extends Triangle {

  protected final Map<Integer, TriangleRow> rows;
  
  
  public SampleTriangleByRow(Ranking reference) {
    super(reference); 
    this.buildReferenceIndexMap();
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < reference.length(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
  }
  
  public SampleTriangleByRow(Ranking reference, RankingSample sample) {
    this(reference);
    
    long start = System.currentTimeMillis();
    for (int item = 1; item < reference.length(); item++) {
      Logger.info("Item %d of %d, %d sec", item, reference.length(), System.currentTimeMillis() - start);
      for (int index = 0; index < sample.size(); index++) {
        PW<Ranking> pw = sample.get(index);
        this.add(pw.p, pw.w, item);
      }
    }
    
  }
  

  @Override
  public TriangleRow getRow(int i) {
    return rows.get(i);
  }
  
  public double get(int item, int pos) {
    return rows.get(item).getProbability(pos);
  }
  
  private void add(Ranking ranking, double weight, int item) {
    Item el = reference.get(item);
    if (!ranking.contains(el)) return;
    
    
    Expands expands = new Expands();
    expands.nullify();
    
    for (int i = 0; i < item; i++) {
      Item e = reference.get(i);
      
      // Ranking mini = upTo(ranking, i);      
      // int pos = mini.indexOf(e);
      
      UpTo upto = new UpTo(ranking, i, referenceIndex);
      int pos = upto.position;
      
      if (pos == -1) { 
        TriangleRow row = rows.get(i);
        expands = expands.insertMissing(row);
      }
      else {
        // Item previous = null;
        // if (pos > 0) previous = mini.get(pos - 1);
        expands = expands.insert(e, upto.previous);      
      }
    }
    
    
    // Ranking mini = upTo(ranking, item);
    // int pos = mini.indexOf(el);            
    // Item previous = null;
    // if (pos > 0) previous = mini.get(pos - 1);
    UpTo upto = new UpTo(ranking, item, referenceIndex);
    expands = expands.insert(el, upto.previous);      
    TriangleRow row = rows.get(item);
    double[] displacements = expands.getDistribution(el);
    for (int j = 0; j < displacements.length; j++) {
      row.inc(j, displacements[j] * weight);
    }

  }
  
  private Map<Item, Integer> referenceIndex = new HashMap<Item, Integer>();
  
  private void buildReferenceIndexMap() {
    referenceIndex.clear();
    for (int i = 0; i < reference.length(); i++) {
      Item e = reference.get(i);
      referenceIndex.put(e, i);
    }    
  }
  
  /** Return the ranking containing only the items up to (and including) max */
  private Ranking upTo(Ranking ranking, int max) {
    Ranking r = new Ranking(ranking.getItemSet());
    for (int i=0; i<ranking.length(); i++) {
      Item e = ranking.get(i);
      int index = referenceIndex.get(e);
      if (index <= max) r.add(e);
    }
    return r;
  }    
  
  @Override
  public int randomPosition(int e) {
    return rows.get(e).getRandomPosition();
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
    ItemSet items = new ItemSet(10);
    Ranking ref = items.getRandomRanking();
    RankingSample sample = MallowsUtils.sample(ref, 0.2, 1000);
    Filter.remove(sample, 0.1);
    
    SampleTriangleByRow t = new SampleTriangleByRow(ref, sample);
    System.out.println(t.equals(t));
    
    SampleTriangle t2 = new SampleTriangle(ref, sample);
    System.out.println(t.equals(t2));
  }
  
}
