package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.sampler.RIMRSampler;
import edu.drexel.cs.db.rank.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.rank.kemeny.KemenyCandidate;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.triangle.SampleTriangleByRow;


public class TestReferenceRandom {

  public static void main(String[] args) {
    int n = 15;
    ItemSet items = new ItemSet(n);    
    double phi = 0.35;
    double missing = 0.2;
    int sampleSize = 3000;
    
    MallowsModel original = new MallowsModel(items.getRandomRanking(), phi);
    System.out.println(original);
    MallowsTriangle triangle = new MallowsTriangle(original);
    RIMRSampler sampler = new RIMRSampler(triangle);
    RankingSample sample = sampler.generate(sampleSize);
    
    CompleteReconstructor reconstructor = new CompleteReconstructor();
    MallowsModel mallows1 = reconstructor.reconstruct(sample);
    System.out.println(mallows1);
    
    Filter.removeItems(sample, missing);
    Ranking candidate = KemenyCandidate.find(sample);
    System.out.println(candidate);
    BubbleTableKemenizator kemenizator = new BubbleTableKemenizator();
    Ranking c1 = kemenizator.kemenize(sample, candidate);
    System.out.println(c1);
    System.out.println("Distance from the original center: " + KendallTauDistance.between(original.getCenter(), c1));
    

    
    SampleTriangleByRow st = new SampleTriangleByRow(c1, sample);
    RIMRSampler resampler = new RIMRSampler(st);
    RankingSample resample = resampler.generate(10000);
    
    MallowsModel mallows = reconstructor.reconstruct(resample);
    System.out.println(mallows);
  }
}
