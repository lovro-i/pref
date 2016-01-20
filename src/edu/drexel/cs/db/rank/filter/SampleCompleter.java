package edu.drexel.cs.db.rank.filter;

import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.util.Histogram;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;

/** Complete each ranking in the sample with a random one(s), consistent with the ranking (the order of the present items is kept) */
public class SampleCompleter {

  private Sample sample;
  
  public SampleCompleter(Sample sample) {
    this.sample = sample;
  }

  /** Completes the incomplete rankings in the sample with random consistent complete one.
   */
  public Sample complete() {
    return complete(1);
  }
  
  
  /** Completes the incomplete rankings in the sample with <code>num</code> random consistent complete ones.
   * Each incomplete ranking is substituted with <code>num</code> complete ones, each with weight <code>1 / num</code>
   */
  public Sample complete(int num) {
    ItemSet items = sample.getItemSet();
    Sample complete = new Sample(items);
    for (int index = 0; index < sample.size(); index++) {
      Ranking r = sample.get(index);
      double weight = sample.getWeight(index);

      int m = items.size() - r.size();
      if (m == 0) {
        complete.add(r, weight);
        continue;
      }
      double w = weight / num;
      for (int i = 0; i < num; i++) {
        Ranking random = items.getRandomRanking();
        int k = 0; // index of item in the incomplete ranking to be inserted next
        for (int j = 0; j < random.size(); j++) {
          Item e = random.get(j);
          if (r.contains(e)) random.set(j, r.get(k++));          
        }
        complete.add(random, w);
      }
    }
    return complete;
  }
  
  public static void testUniformness() {
    int n = 8;
    ItemSet items = new ItemSet(n);
    
    Ranking r = new Ranking(items);
    r.add(items.getItemById(1));
    r.add(items.getItemById(3));
    r.add(items.getItemById(0));

    Histogram<Ranking> hist = new Histogram<Ranking>();
    
    
    for (int i = 0; i < 10000000; i++) {
      Ranking random = items.getRandomRanking();
      int k = 0; // index of item in the incomplete ranking to be inserted next
      for (int j = 0; j < random.size(); j++) {
        Item e = random.get(j);
        if (r.contains(e)) {
          random.set(j, r.get(k++));
        }
      }
      hist.add(random, 1);
    }
    
    System.out.println(hist);
    System.out.println(hist.size());
    System.out.println(SampleTriangle.mixes(r.size(), n-r.size()));
  }

  public static void main(String[] args) { 
    int n = 10;
    ItemSet items = new ItemSet(n);
    
    testUniformness();
    System.exit(0);
    
    Ranking center = items.getReferenceRanking();
    double phi = 0.1;
    
    MallowsTriangle triangle = new MallowsTriangle(center, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);    
    Sample sample = sampler.generate(10);
    Filter.remove(sample, 0.1);
    
    SampleCompleter completer = new SampleCompleter(sample);
    Sample complete = completer.complete(10);
    System.out.println(complete);
  }
  
}
