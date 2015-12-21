
package com.rankst.test;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.ml.TrainUtils;
import com.rankst.model.MallowsModel;
import com.rankst.reconstruct.CompleteReconstructor;
import com.rankst.reconstruct.PolynomialReconstructor;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class PolynomialTests {

  private File folder;
  
  private static int[] sampleSizes = { 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000 };
  private static double[] phis = TrainUtils.step(0.05, 0.95, 0.05);
  private static int[] elems = { 10, 20, 50, 100 };  
  private static int reps = 10;
  
  
  private PolynomialTests(File folder) {
    this.folder = folder;
  }
  
  private void fixElements(int n) throws IOException {
    ElementSet elements = new ElementSet(n);
    PrintWriter out = FileUtils.append(new File(folder, "results.n="+n+".tsv"));
    CompleteReconstructor rec = new CompleteReconstructor();
    
    // elements, sampleSize, real_phi, rec_phi, rel_error, center_error, time_kemeny, time_center, time_phi, time_total, solver_start
    for (int rep = 1; rep <= reps; rep++) {
      for (int sampleSize: sampleSizes) {
        for (double phi: phis) {
          System.out.println(String.format("Pass %d, fixed elements %d, sampleSize %d, phi %2f", rep, n, sampleSize, phi));
          out.print(String.format("%d\t%d\t%.2f", n, sampleSize, phi));
          
          
          /* Das Experiment */
          Ranking reference = elements.getRandomRanking();

          MallowsTriangle triangle = new MallowsTriangle(reference, phi);
          RIMRSampler sampler = new RIMRSampler(triangle);
          Sample sample = sampler.generate(sampleSize);
          MallowsModel mallows = rec.reconstruct(sample);
          
          double recPhi = mallows.getPhi();
          double relErr = Math.abs(recPhi - phi) / phi;
          out.print(String.format("\t%.4f\t%.4f\t", recPhi, relErr));
          
          // center error
          out.print(KendallTauRankingDistance.between(reference, mallows.getCenter()));
          
          // time profiling
          // out.print(String.format("\t%.1f\t", .001 * rec.getTime()));
          
          // solver start
          // out.println(rec.getSolverStart());
          out.flush();
        }
      }      
    }
    
    out.close();
  }
  
  
  private void fixPhi(double phi) throws IOException {
    
    PrintWriter out = FileUtils.append(new File(folder, "results.phi=" + phi + ".tsv"));
    CompleteReconstructor rec = new CompleteReconstructor();
    
    // elements, sampleSize, real_phi, rec_phi, rel_error, center_error, time_kemeny, time_center, time_phi, time_total, solver_start
    for (int rep = 1; rep <= reps; rep++) {
      for (int n: elems) {
        ElementSet elements = new ElementSet(n);
        
        for (int sampleSize: sampleSizes) {
          System.out.println(String.format("Pass %d, elements %d, sampleSize %d, fixed phi %2f", rep, n, sampleSize, phi));
          out.print(String.format("%d\t%d\t%.2f", n, sampleSize, phi));
          
          /* Das Experiment */
          Ranking reference = elements.getRandomRanking();

          MallowsTriangle triangle = new MallowsTriangle(reference, phi);
          RIMRSampler sampler = new RIMRSampler(triangle);
          Sample sample = sampler.generate(sampleSize);
          MallowsModel mallows = rec.reconstruct(sample);
          
          double recPhi = mallows.getPhi();
          double relErr = Math.abs(recPhi - phi) / phi;
          out.print(String.format("\t%.4f\t%.4f\t", recPhi, relErr));
          
          // center error
          out.print(KendallTauRankingDistance.between(reference, mallows.getCenter()));
          
          // time profiling
          // out.print(String.format("\t%.1f\t%.1f\t%.1f\t", .001 * rec.getTimeCenter(), .001 * rec.getTimePhi(), .001 * rec.getTimeTotal()));
          
          // solver start
          // out.println(rec.getSolverStart());
          out.flush();
        }
      }      
    }
    
    out.close();
  }
  
  public static void main(String[] args) throws IOException {
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    PolynomialTests tests = new PolynomialTests(folder);
    
    tests.fixElements(50);
    // tests.fixPhi(0.5);
    
    for (int n: elems) tests.fixElements(n);
    for (double phi: phis) tests.fixPhi(phi);
  }
}
