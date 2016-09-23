package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.posterior.Sequence;
import edu.drexel.cs.db.db4pref.posterior.old.FullExpander;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.util.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExpanderTest {

  private static final double EPSILON = 1e-15;

  @Test
  public void testExpander() {
    ItemSet items = new ItemSet(6); // Create a set of 6 items (elements, alternatives), IDs 0 to 5    
    items.tagLetters(); // Name items by letters (A, B, C...). Otherwise names (tags) of items are their IDs (zero based)
    
    // If you want to custom name items (or to assign any object that items represent), you can use Item.setTag(Object tag)
    // This loop names items sigma_1 to sigma_6. You have it one-based now
    // for (int i = 0; i < items.size(); i++) {
    //   items.get(i).setTag("sigma_" + (i+1));     
    // }
    
    
    double phi = 0.2;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi); // Mallows model defined by its reference ranking and phi
    
    // This is one ranking / partial order
    Ranking r = new Ranking(items);
    r.add(items.getItemById(4));
    r.add(items.getItemById(1));
    r.add(items.getItemById(2));
    
    // You can also access items by name:
    // Item sigma3 = items.getItemByTag("sigma_3");
    
    // Creating an Expander object that will calculate probabilities for this model
    FullExpander expander = new FullExpander(model);
    
    // Get the total probability of ranking r
    Logger.info("Total probability of partial order %s: %f", r, expander.getProbability(r));
    
    { // Build one sequence and ask expander for its probability
      Sequence seq = new Sequence(items); // an empty one
      seq.put(items.get(4), 0); // Putting the item with ID 4 (zero based IDs) at the first (zero indexed) place in the sequence
      seq.put(items.get(1), 2); // Item with id 1 is at index 2
      seq.put(items.get(2), 3);      
      double p = Math.pow(phi, 11) + 2 * Math.pow(phi, 10) + Math.pow(phi, 9) + Math.pow(phi, 5) + Math.pow(phi, 4); // This is the manually calculated correct value that should be obtained
      double t = expander.getProbability(seq);
      Logger.info("Probability of %s: %.7f %.7f", seq, p, t); // Are p and t equal? Yes, they are.
      assertEquals(p, t, EPSILON);
    }
    
    {
      Sequence seq = new Sequence(items);
      seq.put(items.get(1), 2);
      seq.put(items.get(2), 4);
      seq.put(items.get(4), 0);
      double p = Math.pow(phi, 12) + Math.pow(phi, 11) + Math.pow(phi, 9) + Math.pow(phi, 8) + Math.pow(phi, 6) + Math.pow(phi, 5);
      double t = expander.getProbability(seq);
      Logger.info("Probability of %s: %.7f %.7f", seq, p, t);
      assertEquals(p, t, EPSILON);
    }
    
    {
      Sequence seq = new Sequence(items);
      seq.put(items.get(1), 2);
      seq.put(items.get(2), 5);
      seq.put(items.get(4), 0);
      double p = Math.pow(phi, 11) + 2 * Math.pow(phi, 10) + Math.pow(phi, 9) + Math.pow(phi, 7) + Math.pow(phi, 6);
      double t = expander.getProbability(seq);
      Logger.info("Probability of %s: %.7f %.7f", seq, p, t);
      assertEquals(p, t, EPSILON);
    }
  }

}