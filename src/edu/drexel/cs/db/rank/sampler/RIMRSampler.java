package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.triangle.Triangle;
import java.util.List;

/** Creates sample using RIMR, with the insertion probabilities from the given triangle. 
 * Triangle can be created from Mallows model (MallowsTriangle), or from a sample (SampleTriangle)
 */ 
public class RIMRSampler {

  protected Triangle triangle;
  
  public RIMRSampler(Triangle triangle) {
    this.triangle = triangle;
  }
  
  public Ranking generate() {
    Ranking r = new Ranking(triangle.getItemSet());
    List<Item> items = triangle.getReference().getItems();
    
    r.add(items.get(0));
    for (int i=1; i<items.size(); i++) {
      Item e = items.get(i);
      int pos = triangle.randomPosition(i);      
      r.addAt(pos, e);
    }
    return r;
  }
  
  public Sample generate(int count) {
    Sample sample = new Sample(triangle.getItemSet());
    for (int i=0; i<count; i++) {
      Ranking ranking = this.generate();
      sample.add(ranking);
    }
    return sample;
  }
}