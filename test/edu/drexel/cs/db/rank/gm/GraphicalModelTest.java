package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import org.junit.Test;
import static org.junit.Assert.*;

public class GraphicalModelTest {

  public GraphicalModelTest() {
  }

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
  }

}