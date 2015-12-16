package com.rankst.test;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.DirectReconstructor;
import com.rankst.reconstruct.DirectReconstructorSmart;
import com.rankst.triangle.MallowsTriangle;

public class WhySoNegative {

  public static void main(String[] args) {
    ElementSet elements = new ElementSet(30);
    Ranking reference = elements.getReferenceRanking();
    
    double phi = 0.7;
    int sampleSize = 5000;
    
    MallowsTriangle triangle = new MallowsTriangle(reference, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);    

//    APCenterReconstructor apc = new APCenterReconstructor();
//    System.out.println("AP center reconstruction: " + apc.getCenter(sample));
    
    MallowsModel direct = new DirectReconstructor().reconstruct(sample);
    System.out.println("Direct reconstruction center: " + direct.getCenter());
    System.out.println("Direct reconstruction phi: " + direct.getPhi());
  }
  
}
