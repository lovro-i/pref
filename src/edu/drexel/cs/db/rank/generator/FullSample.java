package edu.drexel.cs.db.rank.generator;

import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import java.util.ArrayList;
import java.util.List;

/** Sample containing all rankings of length n */
public class FullSample extends Sample {

  public FullSample(ElementSet elements) {
    super(elements);
    Ranking r = elements.getReferenceRanking();
    permute(r, 0);
  }
  
  public FullSample(ElementSet allElements, List<Element> elements) {
    super(allElements);
    Ranking r = new Ranking(allElements);
    for (Element e: elements) r.add(e);
    permute(r, 0);
  }
  
  public FullSample(Ranking reference) {
    super(reference.getElementSet());
    permute(reference, 0);
  }
  
  private void permute(Ranking r, int k) {
    for (int i = k; i < r.size(); i++) {
      java.util.Collections.swap(r.getElements(), i, k);
      this.permute(r, k + 1);
      java.util.Collections.swap(r.getElements(), k, i);
    }
    if (k == r.size() - 1) {
      Ranking a = new Ranking(r);
      this.add(a);
    }
  }

  public static void main(String[] args) {
    int n = 6;
    ElementSet elements = new ElementSet(n);
    
    Ranking reference = elements.getReferenceRanking();
    Sample sample = new FullSample(reference);
    System.out.println(sample);
    
    List<Element> es = new ArrayList<Element>();
    es.add(elements.get(0));
    // es.add(elements.get(2));
    // es.add(elements.get(3));
    // es.add(elements.get(5));
    Sample s = new FullSample(elements, es);
    System.out.println(s);
    
  }
  
}
