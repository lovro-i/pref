package edu.drexel.cs.db.rank.top;

import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.util.Histogram;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;
import java.util.Set;

/** Complete each ranking in the sample with a random one(s), by randomly appending missing elements to the end */
public class TopSampleCompleter {

  private Sample sample;
  
  public TopSampleCompleter(Sample sample) {
    this.sample = sample;
  }

  /** Completes the incomplete rankings in the sample with random consistent complete one. */
  public Sample complete() {
    return complete(1);
  }
  
  
  /** Completes the incomplete rankings in the sample with <code>num</code> random consistent complete ones.
   * Each incomplete ranking is substituted with <code>num</code> complete ones, each with weight <code>1 / num</code>
   */
  public Sample complete(int num) {
    ItemSet items = sample.getItemSet();
    Sample complete = new Sample(items);
    for (RW rw: sample) {
      
      if (rw.r.size() == items.size()) {
        complete.add(rw.r, rw.w);
        continue;
      }
      
      Set<Item> missingItems = rw.r.getMissingItems();
      Ranking missing = new Ranking(items);
      for (Item i: missingItems) missing.add(i);
      
      double w = rw.w / num;
      for (int i = 0; i < num; i++) {
        Ranking newRanking = new Ranking(rw.r);
        missing.randomize();
        newRanking.add(missing);
        complete.add(newRanking, w);
      }
    }
    return complete;
  }
  


  public static void main(String[] args) { 
    int n = 10;
    ItemSet items = new ItemSet(n);
    
    System.exit(0);
    
    Ranking center = items.getReferenceRanking();
    double phi = 0.1;
    
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);    
    Sample sample = sampler.generate(10);
    Filter.remove(sample, 0.1);
    
    TopSampleCompleter completer = new TopSampleCompleter(sample);
    Sample complete = completer.complete(10);
    System.out.println(complete);
  }
  
}
