package com.rankst.test;

import com.rankst.filter.Filter;
import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.kemeny.BubbleTableKemenizator;
import com.rankst.kemeny.KemenyCandidate;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.CompleteReconstructor;
import com.rankst.reconstruct.PolynomialReconstructor;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import com.rankst.triangle.SampleTriangleByRow;


public class TestReferenceRandom {

  public static void main(String[] args) {
    int n = 15;
    ElementSet elements = new ElementSet(n);    
    double phi = 0.35;
    double missing = 0.2;
    int sampleSize = 3000;
    
    MallowsModel original = new MallowsModel(elements.getRandomRanking(), phi);
    System.out.println(original);
    MallowsTriangle triangle = new MallowsTriangle(original);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    
    CompleteReconstructor reconstructor = new CompleteReconstructor();
    MallowsModel mallows1 = reconstructor.reconstruct(sample);
    System.out.println(mallows1);
    
    Filter.remove(sample, missing);
    Ranking candidate = KemenyCandidate.find(sample);
    System.out.println(candidate);
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
    Ranking c1 = kemenizator.kemenize(sample, candidate);
    System.out.println(c1);
    System.out.println("Distance from the original center: " + KendallTauRankingDistance.between(original.getCenter(), c1));
    

    
    SampleTriangleByRow st = new SampleTriangleByRow(c1, sample);
    RIMRSampler resampler = new RIMRSampler(st);
    Sample resample = resampler.generate(10000);
    
    MallowsModel mallows = reconstructor.reconstruct(resample);
    System.out.println(mallows);
  }
}
