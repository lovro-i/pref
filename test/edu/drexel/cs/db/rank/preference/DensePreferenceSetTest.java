package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
    instance.add(2, 5);
    instance.add(items.get(5), items.get(7));
    assertEquals(3, instance.size());
    
    boolean added = instance.add(items.get(6), items.get(1));
    assertEquals(4, instance.size());
    assertTrue(added);
    
    added = instance.add(items.get(6), items.get(1));
    assertEquals(4, instance.size());
    assertFalse(added);
    
    try {
      instance.add(7, 2);
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
    pref.add(0, 3);
    pref.add(2, 5);
    pref.add(5, 7);
    pref.add(3, 1);
    pref.add(3, 2);
    assertEquals(5, pref.size());
    
    PreferenceSet tc1 = pref.transitiveClosure();
    assertEquals(12, tc1.size());
  }
  
  @Test
  public void testProject() {
    System.out.println("DensePreferenceSet.project() test");
    for (int i = 0; i < 10; i++) {
      Ranking r = items.getRandomRanking();

      DensePreferenceSet tc = new DensePreferenceSet(r);
      Ranking p0 = tc.project(items);
      assertEquals(r, p0);
      
      tc.remove(items.get(2), items.get(4));
      Ranking p1 = tc.project(items);
      assertNull(p1); // should be null because it cannot be projected to a complete ranking

      Set<Item> sub = new HashSet<Item>(items);
      sub.remove(items.get(2));
      sub.remove(items.get(4));
      Ranking p2 = tc.project(sub);
      System.out.println(sub);
      System.out.println(p2);
      assertEquals(8, p2.length());
      
      sub.remove(items.get(0));
      sub.remove(items.get(7));
      Ranking p3 = tc.project(sub);
      System.out.println(sub);
      System.out.println(p3);
      assertEquals(6, p3.length());
    }
  }
      
}
