package edu.drexel.cs.db.db4pref.kemeny;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
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
    p.addById(3, 5);
    p.addById(5, 7);
    p.addById(5, 8);
    p.addById(7, 9);
    
    Ranking result = KemenyCandidate.toRanking(p);
    assertEquals(5, result.length());
    assertEquals(3, result.get(0).id);
    
    for (Preference pref: p.getPreferences()) {
      assertTrue(result.contains(pref));
    }
  }
  
}
