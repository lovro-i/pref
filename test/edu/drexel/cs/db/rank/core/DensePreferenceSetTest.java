package edu.drexel.cs.db.rank.core;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class DensePreferenceSetTest {
  
  private ItemSet items = new ItemSet(10);
  
  public DensePreferenceSetTest() {
  }
  

  /**
   * Test of size method, of class DensePreferenceSet.
   */
  @Test
  public void testSize() {
    System.out.println("DensePreferenceSet.size() test");
    DensePreferenceSet instance = new DensePreferenceSet(items);
    instance.add(items.get(0), items.get(3));
    instance.add(items.get(2), items.get(5));
    instance.add(items.get(5), items.get(7));
    assertEquals(3, instance.size());
  }
  

  @Test  
  public void testAdd() {
    System.out.println("DensePreferenceSet.add() test");
    DensePreferenceSet instance = new DensePreferenceSet(items);
    instance.add(items.get(0), items.get(3));
    instance.addById(2, 5);
    instance.add(items.get(5), items.get(7));
    assertEquals(3, instance.size());
    
    boolean added = instance.add(items.get(6), items.get(1));
    assertTrue(added);
    assertEquals(4, instance.size());
    
    
    added = instance.add(items.get(6), items.get(1));
    assertFalse(added);
    assertEquals(4, instance.size());
    
    
    try {
      instance.addById(7, 2);
      fail();
    }
    catch (IllegalStateException e) {
      // must throw IllegalStateException
    }
    assertEquals(4, instance.size());
  }
  
  
  @Test
  public void testTransitiveClosure() {
    System.out.println("DensePreferenceSet.transitiveClosure() test");
    Ranking r = new Ranking(items);
    r.add(items.get(2));
    r.add(items.get(4));
    r.add(items.get(6));
    r.add(items.get(9));
    r.add(items.get(8));
    
    PreferenceSet tc = r.transitiveClosure();
    assertEquals(10, tc.size());
    
    DensePreferenceSet pref = new DensePreferenceSet(items);
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
    System.out.println("DensePreferenceSet.toRanking() test");
    for (int i = 0; i < 10; i++) {
      Ranking r = items.getRandomRanking();

      DensePreferenceSet tc = new DensePreferenceSet(r);
      Ranking p0 = tc.toRanking(items);
      assertEquals(r, p0);
      
      Item e1 = items.get(2);
      Item e2 = items.get(4);
      tc.remove(e1, e2);
      Ranking p1 = tc.toRanking(items);
      if (Math.abs(r.indexOf(e1) - r.indexOf(e2)) == 1) {
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
