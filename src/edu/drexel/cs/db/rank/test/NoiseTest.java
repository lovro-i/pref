package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;


public class NoiseTest {

  public static void main(String[] args) {
    ElementSet elements = new ElementSet(45);
    Ranking center = elements.getRandomRanking();
    
    double phi = 0.4;
    int sampleSize = 1000;
    
        
    MallowsModel original = new MallowsModel(center, phi);
    System.out.println(original);
    MallowsTriangle triangle = new MallowsTriangle(original);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    
    
    
    CompleteReconstructor direct = new CompleteReconstructor();
    
    MallowsModel mallows = direct.reconstruct(sample);
    System.out.println(mallows);
    System.out.println(KendallTauDistance.between(center, mallows.getCenter()));
    
    Filter.noise(sample, 0.25);
    MallowsModel mallows1 = direct.reconstruct(sample);
    System.out.println(mallows1);
    System.out.println(KendallTauDistance.between(center, mallows1.getCenter()));
  }
}
