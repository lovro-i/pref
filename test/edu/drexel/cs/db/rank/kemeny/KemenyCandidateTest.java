package edu.drexel.cs.db.rank.kemeny;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.Preference;
import org.junit.Test;
import static org.junit.Assert.*;

public class KemenyCandidateTest {
  
  private ItemSet items = new ItemSet(10);

  /**
   * Test of toIncompleteRanking method, of class KemenyCandidate.
   */
  @Test
  public void testToIncompleteRanking() {
    System.out.println("toIncompleteRanking");
    
    MapPreferenceSet p = new MapPreferenceSet(items);
    p.add(3, 5);
    p.add(5, 7);
    p.add(5, 8);
    p.add(7, 9);
    
    Ranking result = KemenyCandidate.toRanking(p);
    assertEquals(5, result.length());
    assertEquals(3, result.get(0).id);
    
    for (Preference pref: p.getPreferences()) {
      assertTrue(result.contains(pref));
    }
  }
  
}
