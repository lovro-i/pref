package edu.drexel.cs.db.rank.filter;

import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.util.Histogram;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;

/** Complete each ranking in the sample with a random one(s), consistent with the ranking (the order of the present items is kept) */
public class SampleCompleter {

  private RankingSample sample;
  
  public SampleCompleter(RankingSample sample) {
    this.sample = sample;
  }

  /** Completes the incomplete rankings in the sample with random consistent complete one.
   */
  public RankingSample complete() {
    return complete(1);
  }
  
  
  /** Completes the incomplete rankings in the sample with <code>num</code> random consistent complete ones.
   * Each incomplete ranking is substituted with <code>num</code> complete ones, each with weight <code>1 / num</code>
   */
  public RankingSample complete(int num) {
    ItemSet items = sample.getItemSet();
    RankingSample complete = new RankingSample(items);
    for (PW<Ranking> pw: sample) {
      int m = items.size() - pw.p.length();
      if (m == 0) {
        complete.add(pw);
        continue;
      }
      double w = pw.w / num;
      for (int i = 0; i < num; i++) {
        Ranking random = items.getRandomRanking();
        int k = 0; // index of item in the incomplete ranking to be inserted next
        for (int j = 0; j < random.length(); j++) {
          Item e = random.get(j);
          if (pw.p.contains(e)) random.set(j, pw.p.get(k++));          
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
      for (int j = 0; j < random.length(); j++) {
        Item e = random.get(j);
        if (r.contains(e)) {
          random.set(j, r.get(k++));
        }
      }
      hist.add(random, 1);
    }
    
    System.out.println(hist);
    System.out.println(hist.size());
    System.out.println(SampleTriangle.mixes(r.length(), n-r.length()));
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
    RankingSample sample = sampler.generate(10);
    Filter.remove(sample, 0.1);
    
    SampleCompleter completer = new SampleCompleter(sample);
    RankingSample complete = completer.complete(10);
    System.out.println(complete);
  }
  
}
