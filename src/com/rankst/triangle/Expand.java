package com.rankst.triangle;

import com.rankst.entity.Element;
import com.rankst.entity.ElementSet;
import com.rankst.util.MathUtils;
import java.util.Arrays;

public class Expand {
  
  private int[] miss;
  private Element[] elements;

  public Expand() {
    this.elements = new Element[0];
    this.miss = new int[1];
  }
  
  private Expand(Element[] elements) {
    this.elements = new Element[elements.length];
    System.arraycopy(elements, 0, this.elements, 0, elements.length);
    miss = new int[elements.length + 1];
  }
  
  private Expand(Expand e) {
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
  
  public Expands insertMissing() {
    Expands expands = new Expands();
    
    // double p = 1;
//    for (int j = 0; j < miss.length; j++) {
//      p *= MathUtils.factorial(miss[j]).doubleValue();        
//    }
    
    for (int i = 0; i < miss.length; i++) {
      Expand ex = new Expand(this);
      ex.miss[i]++;      
      // expands.add(ex, p * ex.miss[i]);
      //expands.add(ex, 1d * ex.miss[i]);
      expands.add(ex, 1d / miss.length);
    }
    expands.normalize(); // treba
    return expands;
  }
  
  public Expands insertMissing(TriangleRow row) {    
    Expands expands = new Expands();
    
    int counter = 0;
    for (int i = 0; i < miss.length; i++) {
      Expand ex = new Expand(this);
      ex.miss[i]++;
      double p = row.getProbability(counter, counter + ex.miss[i]);
      expands.add(ex, p);
      counter += ex.miss[i];
    }
    
    // expands.normalize();
    return expands;
  }
  
  
  /** Adds element e to the right of the element 'prev'.
   *  If (after == null), it is added at the beginning
   */  
  public Expands insert(Element e, Element prev) {
    Expands expands = new Expands();
    
    int index = indexOf(prev);        
    int n = miss[index + 1] + 1;
    //double p = MathUtils.factorial(miss[index + 1]).doubleValue() / n;
    double p = 1d / n;
    
    
    Element[] elements1 = new Element[elements.length + 1];
    for (int i = 0; i < elements1.length; i++) {
      if (i <= index) elements1[i] = elements[i];
      else if (i == index + 1) elements1[i] = e;
      else elements1[i] = elements[i - 1];
    }
    
    for (int i = 0; i < n; i++) {
      Expand ex = new Expand(elements1);
      for (int j = 0; j < ex.miss.length; j++) {
        if (j <= index) ex.miss[j] = this.miss[j];
        else if (j == index + 1) ex.miss[j] = i;
        else if (j == index + 2) ex.miss[j] = this.miss[index + 1] - i;
        else ex.miss[j] = this.miss[j-1];        
      }
      expands.put(ex, p);
    }
    
    expands.normalize(); // treba
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
    if (!(o instanceof Expand)) return false;
    Expand e = (Expand) o;
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
  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(8);
    Element b = elements.getElement(1);
    Element a = elements.getElement(0);
    Element c = elements.getElement(2);
    Element d = elements.getElement(3);
    Element h = elements.getElement(7);
    
    {
      Expands eps = new Expands();
      eps.nullify();
      
      eps = eps.insertMissing();
      eps = eps.insert(b, null);        
      eps = eps.insertMissing();
      eps = eps.insert(d, null);
      //eps = eps.insertMissing();
      
      System.out.println(eps);
      // System.out.println(Arrays.toString(eps.getDistribution(d)));
      System.out.println("\n--------------------------\n\n");
    
    }
    
    System.exit(0);
    
    {
      Expands eps = new Expands();
      eps.nullify();
      eps = eps.insertMissing();
      eps = eps.insert(b, null);
      eps = eps.insert(c, null);
      System.out.println(eps);
      System.out.println("\n--------------------------\n\n");
    }
    
    {
      Expands eps = new Expands();
      eps.nullify();
      eps = eps.insert(a, null);
      eps = eps.insert(b, a);
      eps = eps.insertMissing();
      eps = eps.insert(d, a);
      System.out.println(eps);
      System.out.println("\n--------------------------\n\n");
    }
    
    
    
    
    Element[] es = new Element[2];
    es[0] = b;
    es[1] = a;
    Expand ex = new Expand(es);
    
    Expands expands = ex.insertMissing();
    System.out.println(expands);
    System.out.println();
    
    expands = expands.insertMissing();
    System.out.println(expands);
    System.out.println();
    
    for (Expand exp: expands.keySet()) {
      System.out.println("Expanding "+exp);
      Expands exps = exp.insert(d, b);
      System.out.println(exps);
    }
    
    expands = expands.insert(d, b);
    System.out.println(expands);
    
    
    
//    Expands expands = ex.add(c, b);
//    System.out.println(expands);
    
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
