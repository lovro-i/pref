package com.rankst.reconstruct;

import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.model.MallowsModel;

/** Reconstructs Mallows model using kemenization and PolynomialReconstructor */
public class CompleteReconstructor implements MallowsReconstructor {

  private PolynomialReconstructor reconstructor = new PolynomialReconstructor();

  
  public MallowsModel reconstruct(Sample sample) {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return reconstructor.reconstruct(sample, center);
  }
  
}
