
package edu.drexel.cs.db.db4pref.kemeny;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.sampler.RIMSampler;
import edu.drexel.cs.db.db4pref.util.Histogram;
import edu.drexel.cs.db.db4pref.core.PairwisePreferenceMatrix;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.sampler.triangle.MallowsTriangle;


public class BubbleTableKemenizator implements Kemenizator {

  
  @Override
  public Ranking kemenize(Sample<? extends PreferenceSet> sample, Ranking start) {
    double[][] before = new PairwisePreferenceMatrix(sample).getMatrix();
    Ranking kemeny = new Ranking(start);
    
    boolean foundBetter = true;
    while (foundBetter) {
      foundBetter = false;
      for (int i = 0; i < kemeny.length() - 1; i++) {
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
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(50);
    Ranking center = items.getRandomRanking();
    System.out.println(center);
    MallowsTriangle triangle = new MallowsTriangle(center, 0.97);
    RIMSampler sampler = new RIMSampler(triangle);
    RankingSample sample = sampler.sample(500);
    
    Histogram<Ranking> rankHist = new Histogram(sample);
    Ranking before = rankHist.getMostFrequent();

    long t1 = System.currentTimeMillis();
    Kemenizator kemeny1 = new BubbleTableKemenizator();
    System.out.println(kemeny1.kemenize(sample, before));
    System.out.println(System.currentTimeMillis() - t1);

  }

}
