package com.rankst.triangle;

import com.rankst.entity.Element;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.util.MathUtils;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class SampleTriangle extends Triangle {

  protected final Map<Integer, TriangleRow> rows;
  
  public SampleTriangle(ElementSet elements) {
    super(elements.getReferenceRanking());
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < elements.size(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
  }
  
  public SampleTriangle(Sample sample) {
    this(sample.getElements());
    for (int index = 0; index < sample.size(); index++) {
      Ranking ranking = sample.get(index);
      double weight = sample.getWeight(index);
      this.add(ranking, weight);
    }
    
  }
  
  public TriangleRow getRow(int i) {
    return rows.get(i);
  }
  
  public TriangleRow getRow(Element e) {
    int index = reference.indexOf(e);
    return rows.get(index);
  }
  
  /** Get random position for the element based on added rankings */
  @Override
  public int randomPosition(int e) {
    return rows.get(e).random();
  }
  
  /** Adds ranking to the triangle with specified weight. Returns true if added, false otherwise */
  public boolean add(Ranking ranking, double weight) {
    ElementSet elements = getElements();
    
    Expands expands = new Expands();
    expands.nullify();
    
    for (int i = 0; i < elements.size(); i++) {
      TriangleRow row = rows.get(i); // Triangle row to be updated
      Ranking mini = upTo(ranking, i);
      Element e = elements.getElement(i);
      int pos = mini.indexOf(e);
      
      if (pos == -1) { // This one is missing. Distribute evenly its probability
        expands = expands.insertMissing();
        // double w = 1d / (i + 1);
        // for (int k = 0; k <= i; k++) { row.inc(k, w * weight); }
        continue;
      }
                  
      Element before = null;
      if (pos > 0) before = mini.get(pos - 1);
      expands = expands.insert(e, before);      
      
      double[] displacements = expands.getDistribution(e);
      for (int j = 0; j < displacements.length; j++) {
        row.inc(j, displacements[j] * weight);
      }
      
    }
    return true;
  }

  /** Number of possibilities how the missing ones can be mixed between the fixed ones */
  public static BigInteger mixes(int fixed, int missing) {
    if (missing == 0) return BigInteger.ONE;
    if (fixed < 0) return BigInteger.ZERO;
    if (missing < 0) return BigInteger.ZERO;
    
    BigInteger p = BigInteger.ONE;
    for (int i = 1; i <= missing; i++) {
      p = p.multiply(BigInteger.valueOf(fixed + i));
    }
    return p;
  }
  
  public static void main(String[] args) {
    // System.out.println(mixes(-1, 3));
    ElementSet elements = new ElementSet(8);
    Ranking ranking = new Ranking(elements);
    ranking.add(elements.getElement(1));
    ranking.add(elements.getElement(0));
    ranking.add(elements.getElement(7));
    ranking.add(elements.getElement(3));

    System.out.println(ranking);
    
    Sample sample = new Sample(elements);
    sample.add(ranking);
    
    SampleTriangle triangle = new SampleTriangle(sample);
    System.out.println(triangle);
  }
  
  /** Return the ranking containing only the elements up to (and including) max */
  private static Ranking upTo(Ranking ranking, int max) {
    Ranking r = new Ranking(ranking.getElementSet());
    for (int i=0; i<ranking.size(); i++) {
      Element e = ranking.get(i);
      if (e.getId() <= max) r.add(e);
    }
    return r;
  }
 
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < getElements().size(); i++) {
      sb.append(rows.get(i)).append("\n");
    }  
    return sb.toString();
  }
  
}


