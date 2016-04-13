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
  public void testBatya1() {
    ItemSet items = new ItemSet(25);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.add(3, 7);
    v.add(3, 5);
    v.add(3, 20);
    v.add(5, 2);
    
    GraphicalModel gm = new GraphicalModel(model, v);
    
    gm.alg2();
    // check number of variables after algorithm 2
    assertEquals(5, gm.getVariables().size());
    
    gm.alg3();
    // check number of variables after algorithm 3
    assertEquals(13, gm.getVariables().size());
    
    // check number of edges
    gm.alg4();
    assertEquals(13, gm.getVariables().size());
    assertEquals(17, gm.getEdges().size());
  }
  
  @Test
  public void testBatya2() {
    ItemSet items = new ItemSet(30);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.add(3, 7);
    v.add(3, 5);
    v.add(3, 20);
    v.add(5, 2);
    v.add(20, 25);
    v.add(22, 25);
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.build();

    assertEquals(24, gm.getVariables().size());
    assertEquals(31, gm.getEdges().size());
  }
  
  
  @Test
  public void testBatya3() {
    ItemSet items = new ItemSet(10);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.add(3, 1);
    v.add(3, 2);
    v.add(1, 5);
    v.add(2, 5);
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.build();

    assertEquals(12, gm.getVariables().size());
    assertEquals(16, gm.getEdges().size());
  }
  
}