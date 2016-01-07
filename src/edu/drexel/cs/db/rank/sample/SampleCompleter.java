package edu.drexel.cs.db.rank.sample;

import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.histogram.Histogram;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;

/** Complete each ranking in the sample with a random one(s), consistent with the ranking (the order of the present elements is kept) */
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
    ElementSet elements = sample.getElements();
    Sample complete = new Sample(elements);
    for (int index = 0; index < sample.size(); index++) {
      Ranking r = sample.get(index);
      double weight = sample.getWeight(index);

      int m = elements.size() - r.size();
      if (m == 0) {
        complete.add(r, weight);
        continue;
      }
      double w = weight / num;
      for (int i = 0; i < num; i++) {
        Ranking random = elements.getRandomRanking();
        int k = 0; // index of element in the incomplete ranking to be inserted next
        for (int j = 0; j < random.size(); j++) {
          Element e = random.get(j);
          if (r.contains(e)) random.set(j, r.get(k++));          
        }
        complete.add(random, w);
      }
    }
    return complete;
  }
  
  public static void testUniformness() {
    int n = 8;
    ElementSet elements = new ElementSet(n);
    
    Ranking r = new Ranking(elements);
    r.add(elements.getElement(1));
    r.add(elements.getElement(3));
    r.add(elements.getElement(0));

    Histogram<Ranking> hist = new Histogram<Ranking>();
    
    
    for (int i = 0; i < 10000000; i++) {
      Ranking random = elements.getRandomRanking();
      int k = 0; // index of element in the incomplete ranking to be inserted next
      for (int j = 0; j < random.size(); j++) {
        Element e = random.get(j);
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
    ElementSet elements = new ElementSet(n);
    
    testUniformness();
    System.exit(0);
    
    Ranking center = elements.getReferenceRanking();
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
