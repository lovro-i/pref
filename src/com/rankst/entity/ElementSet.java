package com.rankst.entity;

import com.rankst.util.MathUtils;
import com.rankst.util.Utils;
import java.util.ArrayList;
import java.util.List;


public class ElementSet {

  private final List<Element> elements = new ArrayList<Element>();
  
  public ElementSet(int n) {
    for (int i=0; i<n; i++) elements.add(new Element(i));
  }

 
  public Element getElement(int id) {
    return elements.get(id);
  }
  
  public int size() {
    return elements.size();
  }
  
  public List<Element> getElements() {
    return elements;
  }
  
  /** @return random ranking of length len */
  public Ranking getRandomRanking(int len) {
    if (len > elements.size()) throw new IllegalArgumentException("Ranking length cannot be greater that the number of elements");
    Ranking ranking = new Ranking(this);
    List<Element> elems = new ArrayList<Element>(elements);
    while (ranking.size() < len) {
      int id = MathUtils.RANDOM.nextInt(elems.size());
      Element element = elems.get(id);
      ranking.add(element);
      elems.remove(id);
    }
    return ranking;
  }
  
  /** @return random ranking containing all elements */
  public Ranking getRandomRanking() {
    Ranking ranking = new Ranking(this.getReferenceRanking());
    for (int i = 0; i < ranking.size() - 1; i++) {
      int j = i + MathUtils.RANDOM.nextInt(ranking.size() - i);
      ranking.swap(i, j);
    }
    return ranking;
  }
  
  public Ranking getReferenceRanking() {
    Ranking ranking = new Ranking(this);
    for (Element e: elements) ranking.add(e);
    return ranking;
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof ElementSet)) return false;
    ElementSet elements = (ElementSet) obj;
    if (elements.size() != this.size()) return false;
    
    for (int i = 0; i < this.elements.size(); i++) {
      if (!this.elements.get(i).equals(elements.elements.get(i))) return false;
    }
    return true;
  }
  
}
