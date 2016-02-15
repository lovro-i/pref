package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.Objects;

/** Represents constraints of item at index position in the reference ranking, with only the previous ones */
class PreferenceConstraint extends ArrayList<Boolean> {
  
  
  PreferenceConstraint(PreferenceSet pref, Ranking reference, int index) {
    Item item = reference.get(index);
    for (int i = 0; i < index; i++) {
      Item it = reference.get(i);
      add(pref.isHigher(it, item));
    }
  }

  private Integer hash;
  
  @Override
  public int hashCode() {
    if (hash == null) hash = super.hashCode();
    return hash;
  }

}
