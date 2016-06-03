package edu.drexel.cs.db.db4pref.triangle;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import java.util.Arrays;

public class Expand {
  
  private int[] miss;
  private Item[] items;

  public Expand() {
    this.items = new Item[0];
    this.miss = new int[1];
  }
  
  private Expand(Item[] items) {
    this.items = new Item[items.length];
    System.arraycopy(items, 0, this.items, 0, items.length);
    miss = new int[items.length + 1];
  }
  
  Expand(Expand e) {
    this.items = new Item[e.items.length];
    System.arraycopy(e.items, 0, this.items, 0, items.length);    
    this.miss = new int[e.miss.length];
    System.arraycopy(e.miss, 0, this.miss, 0, miss.length);
  }
  
  /** Returns the length of this ranking (missing + fixed) */
  public int length() {
    int len = this.items.length;
    for (int i = 0; i < miss.length; i++) {
      len += miss[i];      
    }
    return len;
  }
  
  public Expands insertMissing() {
    Expands expands = new Expands();
    
    
    for (int i = 0; i < miss.length; i++) {
      Expand ex = new Expand(this);
      ex.miss[i]++;      
      expands.add(ex, 1d / miss.length);
    }
    expands.normalize();
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
  
  
  /** Adds item e to the right of the item 'prev'.
   *  If (after == null), it is added at the beginning
   */  
  public Expands insert(Item e, Item prev) {
    Expands expands = new Expands();
    
    int index = indexOf(prev);        
    int n = miss[index + 1] + 1;
    //double p = MathUtils.factorial(miss[index + 1]).doubleValue() / n;
    double p = 1d / n;
    
    
    Item[] items1 = new Item[items.length + 1];
    for (int i = 0; i < items1.length; i++) {
      if (i <= index) items1[i] = items[i];
      else if (i == index + 1) items1[i] = e;
      else items1[i] = items[i - 1];
    }
    
    for (int i = 0; i < n; i++) {
      Expand ex = new Expand(items1);
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
  
  /** @returns Index of item e in the array of fixed items */
  private int indexOf(Item e) {
    if (e == null) return -1;
    for (int i = 0; i < items.length; i++) {
      if (e.equals(items[i])) return i;      
    }
    return -1;
  }
  
  /** @return Index of item e in the array of all (fixed + missed) items */
  public int position(Item e) {
    if (e == null) return -1;
    int pos = 0;
    for (int i = 0; i < items.length; i++) {
      pos += miss[i];
      if (e.equals(items[i])) return pos;
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
    
    for (int i = 0; i < items.length; i++) {
      if (!this.items[i].equals(e.items[i])) return false;      
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 73 * hash + Arrays.hashCode(this.miss);
    hash = 73 * hash + Arrays.deepHashCode(this.items);
    return hash;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();    
    sb.append(miss[0]);
    for (int i = 0; i < items.length; i++) {
      sb.append('.').append(items[i]);
      sb.append('.').append(miss[i+1]);      
    }
    return sb.toString();
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(8);
    Item b = items.getItemById(1);
    Item a = items.getItemById(0);
    Item c = items.getItemById(2);
    Item d = items.getItemById(3);
    Item h = items.getItemById(7);
    
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
    
    
    
    
    Item[] es = new Item[2];
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

  /** @return is Item e at position pos */
  public boolean isAt(Item e, int pos) {
    int i = 0;
    for (int j = 0; j < items.length; j++) {
      i += miss[j];
      if (e.equals(items[i])) return i == pos;
      i++;
    }
    return false;
  }
  
  
  
}
