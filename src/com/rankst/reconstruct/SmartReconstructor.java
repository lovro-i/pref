package com.rankst.reconstruct;

import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.incomplete.IncompleteGenerator;
import com.rankst.incomplete.IncompleteReconstructor;
import com.rankst.incomplete.IncompleteUtils;
import com.rankst.model.MallowsModel;
import java.io.File;

/** Decides whether to use Complete or Incomplete reconstructor */
public class SmartReconstructor implements MallowsReconstructor {

  private IncompleteReconstructor incomplete;
  private MallowsReconstructor complete;
  private int trainReps;
  private File arff;
  
  public SmartReconstructor(File arff) throws Exception {
    this(arff, 0);
  }
  
  public SmartReconstructor(File arff, int trainReps) throws Exception {
    this.arff = arff;
    this.incomplete = new IncompleteReconstructor(arff);
    this.trainReps = trainReps;
  }
  
  
  @Override
  public MallowsModel reconstruct(Sample sample) throws Exception {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }
  
  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) throws Exception {
    double missing = IncompleteUtils.getMissingRate(sample);
    boolean isComplete = missing < 0.001;    
    MallowsReconstructor reconstructor;
    if (isComplete) {
      if (complete == null) complete = new CompleteReconstructor();
      reconstructor = complete;
    }
    else {
      if (trainReps > 0) {
        IncompleteGenerator generator = new IncompleteGenerator(arff);
        generator.generateParallel(sample, trainReps);
        incomplete.load();
      }
      reconstructor = incomplete;
    }
    return reconstructor.reconstruct(sample, center);
  }

}
