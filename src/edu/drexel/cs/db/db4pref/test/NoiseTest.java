package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.distance.KendallTauDistance;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.sampler.RIMRSampler;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.db4pref.triangle.MallowsTriangle;


public class NoiseTest {

  public static void main(String[] args) {
    ItemSet items = new ItemSet(45);
    Ranking center = items.getRandomRanking();
    
    double phi = 0.4;
    int sampleSize = 1000;
    
        
    MallowsModel original = new MallowsModel(center, phi);
    System.out.println(original);
    MallowsTriangle triangle = new MallowsTriangle(original);
    RIMRSampler sampler = new RIMRSampler(triangle);
    RankingSample sample = sampler.generate(sampleSize);
    
    
    
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
