package com.rankst.triangle;

import com.rankst.entity.Element;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.util.MathUtils;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/** Sample triangle that should accept incomplete rankings, but that does not count well the positions when there are missing elements
 * To be removed once SampleTriangle class works fine
 * @author Lovro
 */
@Deprecated
public class SampleTriangleBad extends Triangle {

  protected final Map<Integer, TriangleRow> rows;
  
  public SampleTriangleBad(ElementSet elements) {
    super(elements.getReferenceRanking());
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 1; i < elements.size(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
  }
  
  public SampleTriangleBad(Sample sample) {
    this(sample.getElements());
    for (int index = 0; index < sample.size(); index++) {
      Ranking ranking = sample.get(index);
      double weight = sample.getWeight(index);
      this.add(ranking, weight);
    }
  }
  
  /** Get random position for the element based on added rankings */
  @Override
  public int randomPosition(int e) {
    return rows.get(e).random();
  }
  
  @Deprecated
  public void addComplete(Ranking ranking) {
    ElementSet elements = getElements();
    for (int i=1; i<elements.size(); i++) {
      Ranking r = upTo(ranking, i);
      Element e = elements.getElement(i);
      int pos = r.indexOf(e);
      if (pos != -1) {
        TriangleRow c = rows.get(i);
        // System.out.println(r + "; " + i+ "; "+ e + " at " + pos);
        c.inc(pos);
      }
    }
  }
  
  
  public void add(Ranking ranking, double weight) {
    ElementSet elements = getElements();
    for (int i = 1; i < elements.size(); i++) {
      TriangleRow row = rows.get(i); // Triangle row to be updated
      Ranking mini = upTo(ranking, i);
      Element e = elements.getElement(i);
      int pos = mini.indexOf(e);
      if (pos == -1) { // This one is missing. Distribute evenly its probability
        double w = 1d / (i + 1);
        for (int k = 0; k <= i; k++) { row.inc(k, w * weight); }
      }
      else {
        int missing = i + 1 - mini.size(); // This many elements are missing BELOW the current one        
        if (missing == 0) { // No elements missing so far, just increase current position
          row.inc(pos, weight);
        }
        else { // Some elements are missing, so we have to calculate all the possibilities where they could be
          BigInteger[] displacements = new BigInteger[missing + 1];
          BigInteger sum = BigInteger.ZERO;
          for (int before = 0; before <= missing; before++) { // How many of the missing ones we'll put before the current element. Also, that much will it displace (move right) the current element
            int after = missing - before;
            BigInteger c = MathUtils.choose(missing, before).multiply(mixes(pos, before)).multiply(mixes(mini.size() - pos - 1, after));
            displacements[before] = c;
            sum = sum.add(c);
          }
          
          double sumd = sum.doubleValue();
          for (int d=0; d<displacements.length; d++) {
            double w = displacements[d].doubleValue() / sumd;
            row.inc(pos+d, w * weight);
            // Logger.info("Position %d, inc %f, disps[d] %f, sumd %f", pos+d, w, displacements[d].doubleValue(), sumd);
          }
        }
        
      }
    }
  }

  /** Version of add... */
  public void add2(Ranking ranking, double weight) {
    ElementSet elements = getElements();
    for (int i = 1; i < elements.size(); i++) {
      TriangleRow row = rows.get(i); // Triangle row to be updated
      Ranking mini = upTo(ranking, i);
      Element e = elements.getElement(i);
      int pos = mini.indexOf(e);
      
      if (pos == -1) { // This one is missing. Distribute evenly its probability
        double w = 1d / (i + 1);
        for (int k = 0; k <= i; k++) { row.inc(k, w * weight); }
        continue;
      }
            
      int missing = i + 1 - mini.size(); // This many elements are missing BELOW the current one        
      if (missing == 0) { // No elements missing so far, just increase current position
        row.inc(pos, weight);
        continue;
      }
      
      // Now the best part
      // Some elements below this one are missing, so we have to calculate all the possibilities where they could be
      BigInteger[] displacements = new BigInteger[missing + 1];
      for (int j = 0; j < displacements.length; j++) displacements[j] = BigInteger.ZERO;
        
      int fixBefore = pos;
      int fixAfter = mini.size() - pos - 1;
      int missBefore, missMiddle, missAfter;
      
      for (int m = 0; m <= missing; m++) {
        missBefore = missing - m;
        missMiddle = m;
        missAfter = 0;
        
        while (missMiddle >= 0) {
          long nBefore = mixes(fixBefore - 1, missBefore).longValue();          
          long nAfter = mixes(fixAfter - 1, missAfter).longValue();
          if (nBefore > 0 && nAfter > 0) { 
            long nMiddle = MathUtils.factorial(missMiddle).longValue();
            long total = nBefore * nAfter * nMiddle;
            
            int positions = missMiddle + 1;
            long c = total; /// positions;
            for (int shift = 0; shift < positions; shift++) {
              displacements[missBefore + shift] = displacements[missBefore + shift].add(BigInteger.valueOf(c));
            }
          }
          
          missMiddle--;
          missAfter++;
        }
      }           
      
      // Finally, normalize and weighted add to the row
      double sum = 0;
      for (int j = 0; j < displacements.length; j++) {
        sum += displacements[j].doubleValue();        
      }
      System.out.println("sum = " + sum);
      for (int d=0; d<displacements.length; d++) {
        double w = displacements[d].doubleValue() / sum;
        row.inc(pos+d, w * weight);
      }      

    }
  }
  
  /** Number of possibilities how the missing ones can be mixed between the fixed ones */
  public static BigInteger mixes(int fixed, int missing) {
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
    for (int i = 1; i < getElements().size(); i++) {
      sb.append(rows.get(i)).append("\n");
    }  
    return sb.toString();
  }
  
}


