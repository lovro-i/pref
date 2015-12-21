package com.rankst.sample;

import com.rankst.comb.Comb;
import com.rankst.entity.Element;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.histogram.Histogram;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;

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
    for (Ranking r: sample) {
      int m = elements.size() - r.size();
      if (m == 0) {
        complete.add(r);
        continue;
      }
      double w = 1d / num;
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
    Comb.comb(sample, 0.1);
    
    SampleCompleter completer = new SampleCompleter(sample);
    Sample complete = completer.complete(10);
    System.out.println(complete);
  }
  
}
