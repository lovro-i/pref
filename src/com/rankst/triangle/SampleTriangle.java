package com.rankst.triangle;

import com.rankst.entity.Element;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.util.Logger;
import com.rankst.util.MathUtils;
import com.rankst.util.Utils;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class SampleTriangle extends Triangle {

  protected final Map<Integer, TriangleRow> rows;
  
  public SampleTriangle(Ranking reference) {
    super(reference);
    this.buildReferenceIndexMap();
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < reference.size(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
  }
  
  public SampleTriangle(Ranking reference, Sample sample) {
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
    
    Expands expands = new Expands();
    expands.nullify();
    
    for (int i = 0; i < reference.size(); i++) {
      TriangleRow row = rows.get(i); // Triangle row to be updated
      Element e = reference.get(i);
      
      // Ranking mini = upTo(ranking, i);      //old
      // int pos = mini.indexOf(e);            //old
      
      UpTo upto = new UpTo(ranking, i, referenceIndex); //new 
      int pos = upto.position; //new 
      
      if (pos == -1) { // This one is missing. Distribute evenly its probability
        expands = expands.insertMissing();
        // double w = 1d / (i + 1);
        // for (int k = 0; k <= i; k++) { row.inc(k, w * weight); }
        continue;
      }
                  
      // Element previous = null; //old
      // if (pos > 0) previous = mini.get(pos - 1); //old
      // expands = expands.insert(e, previous); //old
      
      expands = expands.insert(e, upto.previous); //new 
      
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
  
  private Map<Element, Integer> referenceIndex = new HashMap<Element, Integer>();
  
  private void buildReferenceIndexMap() {
    referenceIndex.clear();
    for (int i = 0; i < reference.size(); i++) {
      Element e = reference.get(i);
      referenceIndex.put(e, i);
    }    
  }
  
  /** Return the ranking containing only the elements up to (and including) max */
  private Ranking upTo(Ranking ranking, int max) {
    Ranking r = new Ranking(ranking.getElementSet());
    for (int i=0; i<ranking.size(); i++) {
      Element e = ranking.get(i);
      int index = referenceIndex.get(e);
      if (index <= max) r.add(e);
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


