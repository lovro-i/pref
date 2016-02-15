
package edu.drexel.cs.db.rank.kemeny;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.rating.RatingsSample;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.util.Histogram;
import edu.drexel.cs.db.rank.preference.PairwisePreferenceMatrix;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.util.MathUtils;


public class BubbleTableKemenizator implements Kemenizator {

  
  @Override
  public Ranking kemenize(Sample<? extends PreferenceSet> sample, Ranking start) {
    double[][] before = new PairwisePreferenceMatrix(sample).getMatrix();
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
  
  public Ranking kemenize(RatingsSample sample, Ranking start) {
    if (start == null) start = sample.get(MathUtils.RANDOM.nextInt(sample.size())).toRanking();
    
    double[][] before = new PairwisePreferenceMatrix(sample).getMatrix();
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
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(50);
    Ranking center = items.getRandomRanking();
    System.out.println(center);
    MallowsTriangle triangle = new MallowsTriangle(center, 0.97);
    RIMRSampler sampler = new RIMRSampler(triangle);
    RankingSample sample = sampler.generate(500);
    
    Histogram<Ranking> rankHist = new Histogram(sample);
    Ranking before = rankHist.getMostFrequent();

    long t1 = System.currentTimeMillis();
    Kemenizator kemeny1 = new BubbleTableKemenizator();
    System.out.println(kemeny1.kemenize(sample, before));
    System.out.println(System.currentTimeMillis() - t1);

  }

}
