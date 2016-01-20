
package edu.drexel.cs.db.rank.kemeny;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.distance.RankingDistance;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.util.MathUtils;

/** Use if you must, otherwise BubbleTableKemenizator work 1000x faster with the same result */
public class BubbleKemenizator implements Kemenizator {

  private RankingDistance distance;
  
  public BubbleKemenizator(RankingDistance distance) {
    this.distance = distance;
  }

  public BubbleKemenizator() {
    this.distance = KendallTauDistance.getInstance();
  }
  
  
  @Override
  public Ranking kemenize(Sample sample, Ranking start) {
    if (start == null) start = sample.get(MathUtils.RANDOM.nextInt(sample.size()));
    Ranking kemeny = new Ranking(start);
    
    boolean foundBetter = true;
    double totalDistance = getDistance(kemeny, sample);
    while (foundBetter) {
      foundBetter = false;
      for (int i = 0; i < kemeny.size() - 1; i++) {
        kemeny.swap(i, i+1);
        double dist = getDistance(kemeny, sample);
        if (dist < totalDistance) {
          totalDistance = dist;
          foundBetter = true;
          break;
        }
        else {
          kemeny.swap(i, i+1);        
        }
      }
    }
    
    return kemeny;
  }
  
  /** Sum of distances from the given ranking to each one in the sample */
  private double getDistance(Ranking from, Sample sample) {
    double dist = 0;
    for (Sample.RW rw: sample.enumerate()) {
      double d = distance.distance(from, rw.r);
      dist += rw.w * d;
    }
    return dist;
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(30);
    Ranking reference = items.getReferenceRanking();
    
    double phi = 0.8;
    int sampleSize = 5000;
    
    MallowsTriangle triangle = new MallowsTriangle(reference, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    
    
    BubbleKemenizator kemen = new BubbleKemenizator();
    Ranking before = sample.get(MathUtils.RANDOM.nextInt(sampleSize));
    Ranking after = kemen.kemenize(sample, before);
    System.out.println("before = " + before);
    System.out.println("after = " + after);
  }
  
}
