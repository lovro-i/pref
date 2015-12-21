package com.rankst.reconstruct;

import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.kemeny.BubbleTableKemenizator;
import com.rankst.kemeny.KemenyCandidate;

/** Reconstructs center even from the incomplete sample */
public class CenterReconstructor {

  public static Ranking reconstruct(Sample sample) {
    Ranking candidate = KemenyCandidate.find(sample);
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
    return kemenizator.kemenize(sample, candidate);
  }
}
