package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.entity.ElementSet;
import java.util.List;


public class Sequence {

  private ElementSet elements;
  private Element[] sequence;
  
  public Sequence(ElementSet elements) {
    this.elements = elements;
    this.sequence = new Element[elements.size()];
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sequence.length; i++) {
      if (sequence[i] != null) sb.append("sequence[").append(i).append("]: ").append(sequence[i]).append('\n');
    }    
    return sb.toString();
  }
  
  public boolean contains(Element e) {
    for (int i = 0; i < sequence.length; i++) {
      if (e.equals(sequence[i])) return true;
    }
    return false;
  }
  
  public Element[] getElements() {
    return sequence;
  }
  
  public int size() {
    int size = 0;
    for (int i = 0; i < sequence.length; i++) {
      if (sequence[i] != null) size++;
    }
    return size;
  }
  
  
  /** Set the index position of the sequence to be the Element e. 
   * @throws IllegalArgumentException if the element is already at some other place in the sequence
   */ 
  public void set(int index, Element e) {
    if (this.contains(e) && !e.equals(sequence[index])) {
      throw new IllegalArgumentException("Element already in the sequence");
    }    
    sequence[index] = e;
  }
  
  /** An alias for set(pos, e). Put element e at position pos */
  public void put(Element e, int pos) {
    set(pos, e);
  }
  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(10);
    Sequence seq = new Sequence(elements);    
    seq.set(1, elements.get(3));
    seq.set(5, elements.get(7));
    System.out.println(seq);
  }
  
}
