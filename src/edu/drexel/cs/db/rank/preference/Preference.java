package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import java.util.Objects;

/** Two items -- one preference 
 */
public class Preference {

  public final Item higher;
  public final Item lower;
  
  public Preference(Item higher, Item lower) {
    this.higher = higher;
    this.lower = lower;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 47 * hash + Objects.hashCode(this.higher);
    hash = 47 * hash + Objects.hashCode(this.lower);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Preference other = (Preference) obj;
    if (!Objects.equals(this.higher, other.higher)) {
      return false;
    }
    if (!Objects.equals(this.lower, other.lower)) {
      return false;
    }
    return true;
  }

  public boolean contains(Item item) {
    return higher.equals(item) || lower.equals(item);
  }
  
  @Override
  public String toString() {
    return higher + " \u227b " + lower;
  }
  
   
}
