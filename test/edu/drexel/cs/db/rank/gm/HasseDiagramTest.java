package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
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
    prefs.add(0, 1);
    prefs.add(0, 2);
    prefs.add(0, 3);
    prefs.add(0, 4);
    prefs.add(1, 3);
    prefs.add(2, 3);
    prefs.add(2, 4);
    prefs.add(3, 4);
    
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
    prefs.add(10, 4);
    prefs.add(10, 17);
    prefs.add(10, 7);
    prefs.add(17, 20);
    prefs.add(7, 2);
    prefs.add(7, 3);
    
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
}
