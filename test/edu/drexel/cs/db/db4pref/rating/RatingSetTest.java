package edu.drexel.cs.db.db4pref.rating;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class RatingSetTest {

  private ItemSet items = new ItemSet(10);
  
  public RatingSetTest() {
  }


  /**
   * Test of transitiveClosure method, of class RatingSet.
   */
  @Test
  public void testTransitiveClosure() {
    System.out.println("transitiveClosure");
    RatingSet instance = new RatingSet(items);
    instance.put(items.get(1), 3f);
    instance.put(items.get(2), 3f);
    instance.put(items.get(3), 5f);
    instance.put(items.get(4), 7f);
    
    MapPreferenceSet tc = instance.transitiveClosure();
    assertEquals(5, tc.size());
  }

  

}