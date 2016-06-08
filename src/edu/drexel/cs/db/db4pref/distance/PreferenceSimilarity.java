package edu.drexel.cs.db.db4pref.distance;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;

/** Similarity between two PreferenceSets, as the number of same pairs. Used in Affinity Propagation clustering. */
public class PreferenceSimilarity {

  public static double similarity(PreferenceSet pref1, PreferenceSet pref2) {
    ItemSet items = pref1.getItemSet();
    int n = items.size();
    int similarity = 0;
    for (int i = 0; i < n-1; i++) {
      for (int j = i+1; j < n; j++) {
        Boolean b1 = pref1.isPreferred(i, j);
        if (b1 != null) {
          Boolean b2 = pref2.isPreferred(i, j);
          if (b2 != null && b1.equals(b2)) {
            similarity++;
          }
        }
      }      
    }
    return similarity;
  }
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    Ranking r1 = items.getRandomRanking(4);
    Ranking r2 = items.getRandomRanking(4);
    System.out.println(r1);
    System.out.println(r2);
    System.out.println(PreferenceSimilarity.similarity(r1, r2));
    System.out.println(PreferenceSimilarity.similarity(r1.transitiveClosure(), r2.transitiveClosure()));
  }
}
