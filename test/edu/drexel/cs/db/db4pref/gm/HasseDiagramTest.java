package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class HasseDiagramTest {

  public HasseDiagramTest() {
  }

  @Test
  public void test1() {
    System.out.println("HesseDiagramTest 1");
    
    ItemSet items = new ItemSet(5);
    
    MapPreferenceSet prefs = new MapPreferenceSet(items);
    prefs.addById(0, 1);
    prefs.addById(0, 2);
    prefs.addById(0, 3);
    prefs.addById(0, 4);
    prefs.addById(1, 3);
    prefs.addById(2, 3);
    prefs.addById(2, 4);
    prefs.addById(3, 4);
    
    HasseDiagram hasse = new HasseDiagram(prefs);
    
    for (Item item: items) {
      hasse.add(item);
    }
    System.out.println(hasse);
    
    // Check if there are 5 edges after adding all items
    assertEquals(5, hasse.size());
    
    PreferenceSet h = hasse.getPreferenceSet();
    assertTrue(h.contains(0, 1));
    assertTrue(h.contains(2, 3));
    assertTrue(h.contains(3, 4));
    assertTrue(h.contains(0, 2));
    assertTrue(h.contains(1, 3));
  }

  @Test
  public void test2() {
    System.out.println("HasseDiagramTest 2");
  
    ItemSet items = new ItemSet(21);
    
    MapPreferenceSet prefs = new MapPreferenceSet(items);
    prefs.addById(10, 4);
    prefs.addById(10, 17);
    prefs.addById(10, 7);
    prefs.addById(17, 20);
    prefs.addById(7, 2);
    prefs.addById(7, 3);
    
    HasseDiagram hasse = new HasseDiagram(prefs);
    for (int i = 0; i < 11; i++) { // check after adding items up to 10
      Item item = items.get(i);
      hasse.add(item);
    }
    System.out.println(hasse);
    assertEquals(4, hasse.getPreferenceSet().size());
    
    PreferenceSet h = hasse.getPreferenceSet();
    assertTrue(h.contains(10, 4));
    assertTrue(h.contains(10, 7));
    assertTrue(h.contains(7, 3));
    assertTrue(h.contains(7, 2));
  }
  
  
  /** Batya's test: https://www.dropbox.com/s/fuh49hxkubrw8l6/PositionBNExample.pptx?dl=0 */
  @Test
  public void test3() {
    System.out.println("HasseDiagramTest 3");
  
    ItemSet items = new ItemSet(25);
    
    MapPreferenceSet prefs = new MapPreferenceSet(items);
    prefs.addById(3, 7);
    prefs.addById(3, 5);
    prefs.addById(3, 20);
    prefs.addById(5, 2);
    
    HasseDiagram hasse = new HasseDiagram(prefs);
    PreferenceSet h;
    
    hasse.add(items.get(2));
    assertEquals(0, hasse.size());
    h = hasse.getPreferenceSet();
    System.out.println(h);
    
    hasse.add(items.get(3));
    assertEquals(1, hasse.size());
    h = hasse.getPreferenceSet();
    System.out.println(h);
    
    hasse.add(items.get(5));
    assertEquals(2, hasse.size());
    h = hasse.getPreferenceSet();
    System.out.println(h);
    
    hasse.add(items.get(7));
    h = hasse.getPreferenceSet();
    System.out.println(h);
    assertEquals(3, hasse.size());
    assertTrue(h.contains(3, 7));
    assertTrue(h.contains(3, 5));
    assertTrue(h.contains(5, 2));
    
    hasse.add(items.get(20));
    h = hasse.getPreferenceSet();
    System.out.println(h);
    assertEquals(4, hasse.size());    
    assertTrue(h.contains(3, 7));
    assertTrue(h.contains(3, 5));
    assertTrue(h.contains(3, 20));
    assertTrue(h.contains(5, 2));
  }
}
