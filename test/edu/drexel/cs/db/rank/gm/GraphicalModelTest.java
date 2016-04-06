package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class GraphicalModelTest {

  
  @Test
  public void testBuild1() {
    System.out.println("GraphicalModel test build 1");
    
    ItemSet items = new ItemSet(5);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    Ranking r = new Ranking(items);
    r.add(items.get(1));
    r.add(items.get(3));
    
    GraphicalModel gm = new GraphicalModel(model, r);
    gm.build();
    System.out.println(gm);
    assertEquals(3, gm.getVariables().size());
    
    System.out.println(Arrays.toString(gm.getLatest()));
  }

  
  
  @Test
  public void testBuild2() {
    System.out.println("GraphicalModel test build 2");
    
    ItemSet items = new ItemSet(25);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.add(0, 6);
    v.add(0, 9);
    v.add(0, 14);
    v.add(2, 8);
    v.add(2, 12);
    v.add(2, 15);
    v.add(4, 13);
    v.add(4, 20);
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.build();
    System.out.println(gm);
    System.out.println(Arrays.toString(gm.getLatest()));
    
    gm.enhance();
    System.out.println(gm);
  }
}