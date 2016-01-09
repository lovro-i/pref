
package edu.drexel.cs.db.rank.kemeny;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.histogram.Histogram;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.util.MathUtils;


public class BubbleTableKemenizator implements Kemenizator {

  
  @Override
  public Ranking kemenize(Sample sample, Ranking start) {
    if (start == null) start = sample.get(MathUtils.RANDOM.nextInt(sample.size()));
    
    double[][] before = table(sample);    
    Ranking kemeny = new Ranking(start);
    
    boolean foundBetter = true;
    while (foundBetter) {
      foundBetter = false;
      for (int i = 0; i < kemeny.size() - 1; i++) {
        int e1 = kemeny.get(i).getId();
        int e2 = kemeny.get(i+1).getId();        
        if (before[e2][e1] > before[e1][e2]) {
          foundBetter = true;
          kemeny.swap(i, i+1);
        }
        
      }
    }    
    return kemeny;
  }


  /** Creates pairwise table, how many times is element i seen before j */
  public double[][] table(Sample sample) {
    int n = sample.getElements().size();
    double[][] before = new double[n][n];
    
    for (int ri = 0; ri < sample.size(); ri++) {
      Ranking r = sample.get(ri);
      double w = sample.getWeight(ri);
      for (int i = 0; i < r.size()-1; i++) {
        int e1 = r.get(i).getId();
        for (int j = i+1; j < r.size(); j++) {
          int e2 = r.get(j).getId();
          before[e1][e2] += w;
        }
      }
    }
    
    return before;
  }
  
  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(50);
    Ranking center = elements.getRandomRanking();
    System.out.println(center);
    MallowsTriangle triangle = new MallowsTriangle(center, 0.97);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(500);
    
    Histogram<Ranking> rankHist = new Histogram(sample);
    Ranking before = rankHist.getMostFrequent();

    long t1 = System.currentTimeMillis();
    Kemenizator kemeny1 = new BubbleTableKemenizator();
    System.out.println(kemeny1.kemenize(sample, before));
    System.out.println(System.currentTimeMillis() - t1);

    long t2 = System.currentTimeMillis();
    Kemenizator kemeny2 = new BubbleKemenizator();
    System.out.println(kemeny2.kemenize(sample, before));
    System.out.println(System.currentTimeMillis() - t2);
        
  }

}
