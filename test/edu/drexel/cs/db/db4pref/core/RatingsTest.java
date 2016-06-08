package edu.drexel.cs.db.db4pref.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class RatingsTest {

  private ItemSet items = new ItemSet(10);
  
  public RatingsTest() {
  }


  /**
   * Test of transitiveClosure method, of class RatingSet.
   */
  @Test
  public void testTransitiveClosure() {
    System.out.println("transitiveClosure");
    Ratings instance = new Ratings(items);
    instance.put(items.get(1), 3f);
    instance.put(items.get(2), 3f);
    instance.put(items.get(3), 5f);
    instance.put(items.get(4), 7f);
    
    MapPreferenceSet tc = instance.transitiveClosure();
    assertEquals(5, tc.size());
  }

  

}