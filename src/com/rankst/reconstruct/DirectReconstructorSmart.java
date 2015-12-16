
package com.rankst.reconstruct;

import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.histogram.Histogram;

/** Direct reconstructor using most viewed center if > 1, otherwise uses Affinity Propagation to find one */
public class DirectReconstructorSmart extends DirectReconstructor {

  protected Ranking reconstructCenter(Sample sample) {
    Histogram<Ranking> rankHist = new Histogram(sample, sample.getWeights());
    if (rankHist.getMostFrequentCount() > 1.1) return rankHist.getMostFrequent();    
    DirectReconstructorAP ap = new DirectReconstructorAP();
    return ap.reconstructCenter(sample);
  }
  
}
