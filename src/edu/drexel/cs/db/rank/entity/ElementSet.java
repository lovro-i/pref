package edu.drexel.cs.db.rank.entity;

import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** Set of elements (items, alternatives), with id going from 0 to n-1 */
public class ElementSet implements List<Element> {

  private final List<Element> elements = new ArrayList<Element>();
  
  public ElementSet(int n) {
    for (int i=0; i<n; i++) elements.add(new Element(i));
  }
  
  public ElementSet(Object[] objects) {
    for (int i = 0; i < objects.length; i++) {
      Element e = new Element(i, objects[i]);
      elements.add(e);
    }
  }

 
  public Element getElement(int id) {
    return elements.get(id);
  }
  
  public Element getElementByTag(Object tag) {
    for (Element e: elements) {
      if (tag.equals(e.getTag())) return e;
    }
    return null;
  }
  
  @Override
  public int size() {
    return elements.size();
  }
  
  /** Convert the names (tags) of the elements to letters */
  public void letters() {
    for (Element e: elements) {
      e.setTag(String.valueOf(Character.toChars(e.getId()+'A')));
    }
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

  @Override
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return elements.contains(o);
  }

  @Override
  public Iterator<Element> iterator() {
    return elements.iterator();
  }

  @Override
  public Object[] toArray() {
    return elements.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return elements.toArray(a);
  }

  @Override
  public boolean add(Element e) {    
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return elements.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends Element> c) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean addAll(int index, Collection<? extends Element> c) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public Element get(int index) {
    return elements.get(index);
  }

  @Override
  public Element set(int index, Element element) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public void add(int index, Element element) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public Element remove(int index) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public int indexOf(Object o) {
    return elements.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return elements.lastIndexOf(o);
  }

  @Override
  public ListIterator<Element> listIterator() {
    return elements.listIterator();
  }

  @Override
  public ListIterator<Element> listIterator(int index) {
    return elements.listIterator(index);
  }

  @Override
  public List<Element> subList(int fromIndex, int toIndex) {
    return elements.subList(fromIndex, toIndex);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.size()).append(" elements: { ");
    for (int i = 0; i < elements.size(); i++) {
      if (i > 0) sb.append(", ");
      sb.append(elements.get(i));
    }
    sb.append(" }");
    return sb.toString();
  }
}
