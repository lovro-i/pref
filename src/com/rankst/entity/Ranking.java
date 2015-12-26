package com.rankst.entity;

import java.util.*;


public class Ranking implements Comparable {

  private static final Random random = new Random();
  private static final String DELIMITER = "-";
  
  private ElementSet elementSet;
  private List<Element> elements = new ArrayList<Element>();
  
  public Ranking(ElementSet elementSet) {
    this.elementSet = elementSet;
  }
  
  public Ranking(Ranking ranking) {
    this.elementSet = ranking.elementSet;
    this.elements.addAll(ranking.elements);
  }

  public ElementSet getElementSet() {
    return elementSet;
  }
  
  public Set<Element> getMissingElements() {
    Set<Element> missing = new HashSet<Element>();
    for (Element e: elementSet.getElements()) {
      if (!this.contains(e)) missing.add(e);
    }
    return missing;
  }
  
  /** Add Element e at the end of the ranking */
  public Ranking add(Element e) {
    if (this.contains(e)) throw new IllegalArgumentException("Element already in the sample");
    elements.add(e);
    return this;
  }
  
  public Element set(int index, Element e) {
    return elements.set(index, e);
  }
  
  /** Add Element e at the specified position in the ranking (shifting the ones on the right). If index >= size of the element, add at the end */
  public Ranking addAt(int index, Element e) {
    if (index >= elements.size()) elements.add(e);
    else elements.add(index, e);
    return this;
  }

  public Ranking remove(int index) {
    elements.remove(index);
    return this;
  }
  
  
  /** Add Element e at the random position in the ranking */
  public void addAtRandom(Element e) {
    int index = random.nextInt(elements.size()+1);
    this.addAt(index, e);
  }
  
  
  public List<Element> getElements() {
    return elements;
  }
  
  public boolean contains(Element e) {
    return elements.contains(e);
  }
  
  /** Number of elements in this ranking. */
  public int size() {
    return elements.size();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<elements.size(); i++) {
      if (i != 0) sb.append(DELIMITER);
      sb.append(elements.get(i));
    }
    return sb.toString();
  }

  public void swap(int i1, int i2) {
    Element e1 = elements.get(i1);
    Element e2 = elements.get(i2);
    elements.set(i1, e2);
    elements.set(i2, e1);
  }

  /** Return the element at i-th place in the ranking */
  public Element get(int i) {
    return elements.get(i);
  }
  
  /** Returns the index of the given element, -1 if it's not in the ranking */
  public int indexOf(Element e) {
    return elements.indexOf(e);
  }
  
  @Override
  public boolean equals(Object o) {
    Ranking ranking = (Ranking) o;
    if (this.elements.size() != ranking.elements.size()) return false;
    for (int i=0; i<elements.size(); i++) {
      if (!this.elements.get(i).equals(ranking.elements.get(i))) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.elements);
    return hash;
  }
  
  public static Ranking parse(ElementSet elements, String s) {
    Ranking ranking = new Ranking(elements);
    StringTokenizer st = new StringTokenizer(s, DELIMITER);
    while(st.hasMoreTokens()) {
      String t = st.nextToken();
      int id = Integer.parseInt(t);
      Element e = new Element(id);
      ranking.add(e);
    }
    return ranking;
  }    

  @Override
  public int compareTo(Object o) {
    return this.toString().compareTo(o.toString());
  }
  
}
 