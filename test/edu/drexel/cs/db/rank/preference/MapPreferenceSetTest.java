package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
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
    pref.add(0, 3);
    pref.add(2, 5);
    pref.add(5, 7);
    pref.add(3, 1);
    pref.add(3, 2);
    assertEquals(5, pref.size());
    
    PreferenceSet tc1 = pref.transitiveClosure();
    assertEquals(12, tc1.size());
  }
  
//
//  /**
//   * Test of prune method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testPrune() {
//    System.out.println("prune");
//    MapPreferenceSet instance = null;
//    instance.prune();
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of add method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testAdd_Item_Item() {
//    System.out.println("add");
//    Item higher = null;
//    Item lower = null;
//    MapPreferenceSet instance = null;
//    boolean expResult = false;
//    boolean result = instance.add(higher, lower);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of add method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testAdd_int_int() {
//    System.out.println("add");
//    int higherId = 0;
//    int lowerId = 0;
//    MapPreferenceSet instance = null;
//    boolean expResult = false;
//    boolean result = instance.add(higherId, lowerId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of remove method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testRemove_Item_Item() {
//    System.out.println("remove");
//    Item item1 = null;
//    Item item2 = null;
//    MapPreferenceSet instance = null;
//    Boolean expResult = null;
//    Boolean result = instance.remove(item1, item2);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of remove method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testRemove_int_int() {
//    System.out.println("remove");
//    int itemId1 = 0;
//    int itemId2 = 0;
//    MapPreferenceSet instance = null;
//    Boolean expResult = null;
//    Boolean result = instance.remove(itemId1, itemId2);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getItemSet method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testGetItemSet() {
//    System.out.println("getItemSet");
//    MapPreferenceSet instance = null;
//    ItemSet expResult = null;
//    ItemSet result = instance.getItemSet();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of isPreferred method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testIsPreferred_Item_Item() {
//    System.out.println("isPreferred");
//    Item preferred = null;
//    Item over = null;
//    MapPreferenceSet instance = null;
//    Boolean expResult = null;
//    Boolean result = instance.isPreferred(preferred, over);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of isPreferred method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testIsPreferred_int_int() {
//    System.out.println("isPreferred");
//    int preferred = 0;
//    int over = 0;
//    MapPreferenceSet instance = null;
//    Boolean expResult = null;
//    Boolean result = instance.isPreferred(preferred, over);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of transitiveClose method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testTransitiveClose() {
//    System.out.println("transitiveClose");
//    MapPreferenceSet instance = null;
//    instance.transitiveClose();
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of transitiveClosure method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testTransitiveClosure() {
//    System.out.println("transitiveClosure");
//    MapPreferenceSet instance = null;
//    MapPreferenceSet expResult = null;
//    MapPreferenceSet result = instance.transitiveClosure();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of project method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testProject() {
//    System.out.println("project");
//    Collection<Item> items = null;
//    MapPreferenceSet instance = null;
//    Ranking expResult = null;
//    Ranking result = instance.project(items);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getHigher method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testGetHigher() {
//    System.out.println("getHigher");
//    Item i = null;
//    MapPreferenceSet instance = null;
//    Set<Item> expResult = null;
//    Set<Item> result = instance.getHigher(i);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getLower method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testGetLower() {
//    System.out.println("getLower");
//    Item i = null;
//    MapPreferenceSet instance = null;
//    Set<Item> expResult = null;
//    Set<Item> result = instance.getLower(i);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of contains method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testContains_Item_Item() {
//    System.out.println("contains");
//    Item higher = null;
//    Item lower = null;
//    MapPreferenceSet instance = null;
//    boolean expResult = false;
//    boolean result = instance.contains(higher, lower);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of contains method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testContains_int_int() {
//    System.out.println("contains");
//    int higherId = 0;
//    int lowerId = 0;
//    MapPreferenceSet instance = null;
//    boolean expResult = false;
//    boolean result = instance.contains(higherId, lowerId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of contains method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testContains_Item() {
//    System.out.println("contains");
//    Item item = null;
//    MapPreferenceSet instance = null;
//    boolean expResult = false;
//    boolean result = instance.contains(item);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of clone method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testClone() {
//    System.out.println("clone");
//    MapPreferenceSet instance = null;
//    MapPreferenceSet expResult = null;
//    MapPreferenceSet result = instance.clone();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getPreferences method, of class MapPreferenceSet.
//   */
//  @Test
//  public void testGetPreferences() {
//    System.out.println("getPreferences");
//    MapPreferenceSet instance = null;
//    Set<Preference> expResult = null;
//    Set<Preference> result = instance.getPreferences();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//  
}
