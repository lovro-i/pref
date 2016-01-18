package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.Arrays;

/** One state */
public class MallowsExpand {
  
  private Expander expander;
  private int[] miss;
  private Element[] elements;

  public MallowsExpand(Sequence seq) {
    this.elements = new Element[seq.size()];
    this.miss = new int[this.elements.length + 1];
    
    int ie = 0;
    int im = 0;
    Element[] s = seq.getElements();
    for (int i = 0; i < s.length; i++) {
      if (s[i] == null) this.miss[im]++;
      else {
        this.elements[ie] = s[i];
        ie++;
        im++;
      }
    }
  }
  
  public MallowsExpand(Expander expander) {
    this.expander = expander;
    this.elements = new Element[0];
    this.miss = new int[1];
  }
  
  private MallowsExpand(Expander expander, Element[] elements) {
    this.expander = expander;
    this.elements = new Element[elements.length];
    System.arraycopy(elements, 0, this.elements, 0, elements.length);
    miss = new int[elements.length + 1];
  }
  
  private MallowsExpand(Expander expander, MallowsExpand e) {
    this.expander = expander;
    this.elements = new Element[e.elements.length];
    System.arraycopy(e.elements, 0, this.elements, 0, elements.length);    
    this.miss = new int[e.miss.length];
    System.arraycopy(e.miss, 0, this.miss, 0, miss.length);
  }
  
  /** Returns the length of this ranking (missing + fixed) */
  public int length() {
    int len = this.elements.length;
    for (int i = 0; i < miss.length; i++) {
      len += miss[i];      
    }
    return len;
  }
  
  public MallowsExpands insertMissing(Element e) {
    MallowsExpands expands = new MallowsExpands(expander);
    
    int pos = 0;
    for (int i = 0; i < miss.length; i++) {
      MallowsExpand ex = new MallowsExpand(expander, this);
      ex.miss[i]++;      
      
      double p = 0;
      for (int j = 0; j <= miss[i]; j++) {
        p += probability(e.getId(), pos);
        pos++;
      }
      expands.add(ex, p);
    }
    // expands.normalize();
    return expands;
  }
  

  private double probability(int elementIndex, int position) {
    double phi = expander.getModel().getPhi();
    double r = Math.pow(phi, Math.abs(elementIndex - position));
    return r;
  }
  
  
  /** Adds element e to the right of the element 'prev'.
   *  If (after == null), it is added at the beginning
   */  
  public MallowsExpands insert(Element e, Element prev) {
    MallowsExpands expands = new MallowsExpands(expander);
    
    int index = indexOf(prev); // index of the previous element
    int n = miss[index + 1] + 1; // how many are missing before the previous and the next, plus one: the number of different new expand states
        
    
    int posPrev = index;
    for (int i = 0; i <= index; i++) {
      posPrev += miss[i];      
    }
    
    // create new array of elements, by inserting it after the previous
    Element[] elements1 = new Element[elements.length + 1];
    for (int i = 0; i < elements1.length; i++) {
      if (i <= index) elements1[i] = elements[i];
      else if (i == index + 1) elements1[i] = e;
      else elements1[i] = elements[i - 1];
    }
    
    // create n new expand states with their probabilities    
    for (int i = 0; i < n; i++) {
      MallowsExpand ex = new MallowsExpand(expander, elements1);
      for (int j = 0; j < ex.miss.length; j++) {
        if (j <= index) ex.miss[j] = this.miss[j];
        else if (j == index + 1) ex.miss[j] = i;
        else if (j == index + 2) ex.miss[j] = this.miss[index + 1] - i;
        else ex.miss[j] = this.miss[j-1];        
      }
      double p = probability(e.getId(), posPrev + 1 + i);
      expands.put(ex, p);
    }
    
    // expands.normalize(); // treba
    return expands;
  }
  
  /** @returns Index of element e in the array of fixed elements */
  private int indexOf(Element e) {
    if (e == null) return -1;
    for (int i = 0; i < elements.length; i++) {
      if (e.equals(elements[i])) return i;      
    }
    return -1;
  }
  
  /** @returns Index of element e in the array of all (fixed + missed) elements */
  public int position(Element e) {
    if (e == null) return -1;
    int pos = 0;
    for (int i = 0; i < elements.length; i++) {
      pos += miss[i];
      if (e.equals(elements[i])) return pos;
      pos++;
    }
    return -1;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof MallowsExpand)) return false;
    MallowsExpand e = (MallowsExpand) o;
    if (this.miss.length != e.miss.length) return false;
    for (int i = 0; i < miss.length; i++) {
      if (this.miss[i] != e.miss[i]) return false;      
    }
    
    for (int i = 0; i < elements.length; i++) {
      if (!this.elements[i].equals(e.elements[i])) return false;      
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 73 * hash + Arrays.hashCode(this.miss);
    hash = 73 * hash + Arrays.deepHashCode(this.elements);
    return hash;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();    
    sb.append(miss[0]);
    for (int i = 0; i < elements.length; i++) {
      sb.append('.').append(elements[i]);
      sb.append('.').append(miss[i+1]);      
    }
    return sb.toString();
  }
  

  /** @return is Element e at position pos */
  public boolean isAt(Element e, int pos) {
    int i = 0;
    for (int j = 0; j < elements.length; j++) {
      i += miss[j];
      if (e.equals(elements[i])) return i == pos;
      i++;
    }
    return false;
  }
  
}
