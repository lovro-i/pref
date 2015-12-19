
package com.rankst.reconstruct;

import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.histogram.Histogram;
import com.rankst.kemeny.BubbleKemenizator;


public class DirectReconstructorKemeny extends DirectReconstructor {
  
  protected Ranking reconstructCenter(Sample sample) {
    Histogram<Ranking> rankHist = new Histogram(sample, sample.getWeights());
    Ranking before = rankHist.getMostFrequent();
    BubbleKemenizator kemenizator = new BubbleKemenizator();
    Ranking after = kemenizator.kemenize(sample, before);
    return after;
  }
  
}
