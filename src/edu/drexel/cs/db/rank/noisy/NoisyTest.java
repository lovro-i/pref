package edu.drexel.cs.db.rank.noisy;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.io.File;


public class NoisyTest {

  public static void randomTest() throws Exception {
    // Elements
    int n = 20; // MathUtils.RANDOM.nextInt(30) + 10;
    ElementSet elements = new ElementSet(n);
    
    // Mallows Model
    Ranking center = elements.getRandomRanking();
    double phi = 0.35; // MathUtils.RANDOM.nextDouble() * 0.8 + 0.05;
    MallowsModel model = new MallowsModel(center, phi);
    
    // Sample
    int sampleSize = 5000; // (MathUtils.RANDOM.nextInt(95) + 5) * 100;
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    
    // Noise
    double noise = 0.3; // MathUtils.RANDOM.nextDouble() * 0.85;
    Filter.noise(sample, noise);
    
    Logger.info("\n\n[Test] %d elements, %d rankings in sample, phi = %.3f, noise = %.2f", n, sample.size(), phi, noise);
    
    
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    File arff = new File(folder, "noisy.train.arff");
        
    NoisyGenerator generator = new NoisyGenerator(arff);
    generator.generateParallel(sample, 2);
    
    NoisyReconstructor reconstructor = new NoisyReconstructor(arff);
    MallowsModel reconstructed = reconstructor.reconstruct(sample);
    
    Logger.info("\n\n[Test] %d elements, %d rankings in sample, phi = %.3f, noise = %.2f", n, sampleSize, phi, noise);
    Logger.info("[Original model]      " + model);
    Logger.info("[Reconstructed model] " + reconstructed);
  }
  
  public static void main(String[] args) throws Exception {
    randomTest();
  }
}
