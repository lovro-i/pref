package com.rankst.incomplete;

import com.rankst.comb.Comb;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.ml.RegressionReconstructor;
import com.rankst.model.MallowsModel;
import com.rankst.sample.SampleCompleter;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import java.io.File;


public class IncompleteTest1 {

  public static void main(String[] args) throws Exception {
    int n = 10;
    ElementSet elements = new ElementSet(n);
    Ranking center = elements.getReferenceRanking();
    
    for (double phi = 0.1; phi < 0.62; phi += 0.1) {
      MallowsTriangle triangle = new MallowsTriangle(center, phi);
      RIMRSampler sampler = new RIMRSampler(triangle);    
      Sample sample = sampler.generate(10000);

      Comb.comb(sample, 0.1);
//      SampleTriangle st = new SampleTriangle(sample);
//      RIMRSampler resampler = new RIMRSampler(st);
//      Sample resample = resampler.generate(10000);

      SampleCompleter completer = new SampleCompleter(sample);
      Sample resample = completer.complete(1);
      
      File folder = new File("C:\\Projects\\Rankst\\Results2");
      RegressionReconstructor reconstructor = new RegressionReconstructor(new File(folder, "train.arff"));
      MallowsModel reconstructedModel = reconstructor.reconstruct(resample);

      System.out.println(String.format("%.3f -> %.3f", phi, reconstructedModel.getPhi()));  
    }
    
    
    
  }
}
