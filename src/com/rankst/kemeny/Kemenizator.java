package com.rankst.kemeny;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.distance.RankingDistance;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;

public interface Kemenizator {

  public Ranking kemenize(Sample sample, Ranking start);
  
}
