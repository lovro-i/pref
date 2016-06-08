package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.sampler.triangle.Triangle;
import java.util.List;

/** Creates sample using RIMR, with the insertion probabilities from the given triangle. 
 * Triangle can be created from Mallows model (MallowsTriangle), or from a sample (SampleTriangle)
 */ 
public class RIMSampler {

  protected Triangle triangle;
  
  public RIMSampler(Triangle triangle) {
    this.triangle = triangle;
  }
  
  public Ranking sample() {    
    List<Item> items = triangle.getReference().getItems();
    
    Ranking r = new Ranking(triangle.getItemSet());
    r.add(items.get(0));
    for (int i=1; i<items.size(); i++) {
      Item e = items.get(i);
      int pos = triangle.randomPosition(i);      
      r.add(pos, e);
    }
    return r;
  }
  
  public RankingSample sample(int count) {
    RankingSample sample = new RankingSample(triangle.getItemSet());
    for (int i=0; i<count; i++) {
      Ranking ranking = this.sample();
      sample.add(ranking);
    }
    return sample;
  }
}
