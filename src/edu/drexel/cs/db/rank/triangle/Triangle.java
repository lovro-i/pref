package edu.drexel.cs.db.rank.triangle;

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
  
  
  
}
