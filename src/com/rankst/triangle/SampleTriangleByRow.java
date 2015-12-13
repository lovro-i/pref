
package com.rankst.triangle;

import com.rankst.entity.Element;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import java.util.HashMap;
import java.util.Map;

/** Fill the triangle element by element first, not by rankings */
public class SampleTriangleByRow extends Triangle {

  protected final Map<Integer, TriangleRow> rows;
  
  protected final ElementSet elements;
  
  public SampleTriangleByRow(ElementSet elements) {
    super(elements.getReferenceRanking());
    this.elements = elements;
    
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < elements.size(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
  }
  
  public SampleTriangleByRow(Sample sample) {
    this(sample.getElements());
    
    for (int element = 1; element < elements.size(); element++) {    
                      
      for (int index = 0; index < sample.size(); index++) {
        Ranking ranking = sample.get(index);
        double weight = sample.getWeight(index);
        this.add(ranking, weight, element);
      }

      //System.out.println(this);
    }
    
  }
  
  
  private void add(Ranking ranking, double weight, int element) {
    Element el = elements.getElement(element);
    if (!ranking.contains(el)) return;
    
    
    Expands expands = new Expands();
    expands.nullify();
    
    for (int i = 0; i < element; i++) {       
      Ranking mini = upTo(ranking, i);
      Element e = elements.getElement(i);
      int pos = mini.indexOf(e);
      
      if (pos == -1) { 
        TriangleRow row = rows.get(i);
        expands = expands.insertMissing(row);
      }
      else {
        Element previous = null;
        if (pos > 0) previous = mini.get(pos - 1);
        expands = expands.insert(e, previous);      
      }
    }
    
    
    Ranking mini = upTo(ranking, element);
    int pos = mini.indexOf(el);            
    Element previous = null;
    if (pos > 0) previous = mini.get(pos - 1);
    expands = expands.insert(el, previous);      
    TriangleRow row = rows.get(element);
    double[] displacements = expands.getDistribution(el);
    for (int j = 0; j < displacements.length; j++) {
      row.inc(j, displacements[j] * weight);
    }

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
  public int randomPosition(int e) {
    return rows.get(e).random();
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
