package edu.drexel.cs.db.db4pref.core;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapPreferenceSetTest {

  private ItemSet items = new ItemSet(10);

  public MapPreferenceSetTest() {
  }

  /**
   * Test of size method, of class MapPreferenceSet.
   */
  @Test
  public void testSize() {
    System.out.println("MapPreferenceSet.size() test");
    MapPreferenceSet instance = new MapPreferenceSet(items);
    instance.add(items.get(0), items.get(3));
    instance.add(items.get(2), items.get(5));
    instance.add(items.get(5), items.get(7));
    assertEquals(3, instance.size());
  }

  @Test
  public void testAdd() {
    System.out.println("MapPreferenceSet.add() test");
    MapPreferenceSet instance = new MapPreferenceSet(items);
    instance.add(items.get(0), items.get(3));
    instance.addById(2, 5);
    instance.add(items.get(5), items.get(7));
    assertEquals(3, instance.size());

    boolean added = instance.add(items.get(6), items.get(1));
    assertEquals(4, instance.size());
    assertTrue(added);

    added = instance.add(items.get(6), items.get(1));
    assertEquals(4, instance.size());
    assertFalse(added);

    try {
      instance.addById(7, 2);
      fail();
    } catch (IllegalStateException e) {
      // must throw IllegalStateException
    }
    assertEquals(4, instance.size());
  }
  
  
  @Test
  public void testAdd2() {
    System.out.println("MapPreferenceSet.add() test 2");
    MapPreferenceSet instance = new MapPreferenceSet(items);
    instance.addById(0, 3);
    try {
      instance.addById(3, 0);
      fail();
    } catch (IllegalStateException e) {
      // must throw IllegalStateException
    }
  }

  @Test
  public void testTransitiveClosure() {
    System.out.println("MapPreferenceSet.transitiveClosure() test");
    Ranking r = new Ranking(items);
    r.add(items.get(2));
    r.add(items.get(4));
    r.add(items.get(6));
    r.add(items.get(9));
    r.add(items.get(8));

    PreferenceSet tc = r.transitiveClosure();
    assertEquals(10, tc.size());

    MapPreferenceSet pref = new MapPreferenceSet(items);
    pref.addById(0, 3);
    pref.addById(2, 5);
    pref.addById(5, 7);
    pref.addById(3, 1);
    pref.addById(3, 2);
    assertEquals(5, pref.size());

    PreferenceSet tc1 = pref.transitiveClosure();
    assertEquals(12, tc1.size());
  }

  @Test
  public void testToRanking() {
    System.out.println("MapPreferenceSet.toRanking() test");
    for (int i = 0; i < 10; i++) {
      Ranking r = items.getRandomRanking();

      MapPreferenceSet tc = r.transitiveClosure();
      System.out.println(tc);
      Ranking p0 = tc.toRanking(items);
      assertEquals(r, p0);

      Item e1 = items.get(2);
      Item e2 = items.get(4);
      int idx1 = r.getIndexMap().get(e1);
      int idx2 = r.getIndexMap().get(e2);
      tc.remove(e1, e2);
      Ranking p1 = tc.toRanking(items);
      if (Math.abs(idx1 - idx2) == 1) {
        assertNull(p1); // should be null because it cannot be projected to a complete ranking (a pair is missing)
      } else {
        assertNotNull(p1); 
        assertEquals(r, p1);
      }

      Set<Item> sub = new HashSet<Item>(items);
      sub.remove(items.get(2));
      sub.remove(items.get(4));
      Ranking p2 = tc.toRanking(sub);
      System.out.println(sub);
      System.out.println(p2);
      assertNotNull(p2);
      assertEquals(8, p2.length());

      sub.remove(items.get(0));
      sub.remove(items.get(7));
      Ranking p3 = tc.toRanking(sub);
      System.out.println(sub);
      System.out.println(p3);
      assertEquals(6, p3.length());
    }
  }

}
