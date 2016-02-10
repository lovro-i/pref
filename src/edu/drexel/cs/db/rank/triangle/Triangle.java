package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import java.security.SecureRandom;
import java.util.Random;


public abstract class Triangle {

  public static final Random random = new SecureRandom();
  
  protected final Ranking reference;
  
  public Triangle(Ranking reference) {
    this.reference = reference;    
  }
  
  public ItemSet getItemSet() {
    return this.reference.getItemSet();
  }

  public Ranking getReference() {
    return reference;
  } 
  
  /** Get random position for the item e from the triangle */
  public abstract int randomPosition(int e);
  
  public abstract double get(int item, int pos);
  
  public abstract TriangleRow getRow(int item);
  
  
  public TriangleRow getRow(Item e) {
    int index = reference.indexOf(e);
    return getRow(index);
  }
    
  public boolean equals(Object o) {
    if (!(o instanceof Triangle)) return false;
    Triangle t = (Triangle) o;
    double epsilon = 0.0001;
    for (int item = 0; item < reference.size(); item++) {      
      for (int pos = 0; pos <= item; pos++) {
        double diff = Math.abs(this.get(item, pos) - t.get(item, pos)); 
        if (diff > epsilon) return false;
      }      
    }    
    return true;
  }
  
}
