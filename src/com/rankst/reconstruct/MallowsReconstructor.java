package com.rankst.reconstruct;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.model.MallowsModel;
import com.rankst.triangle.MallowsTriangle;


public interface MallowsReconstructor {
  
  public MallowsModel reconstruct(Sample sample) throws Exception;
  

}
