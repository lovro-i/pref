
package edu.drexel.cs.db.rank.reconstruct;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.distance.KendallTauUtils;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.MallowsTriangle;
import edu.drexel.cs.db.rank.util.SystemOut;
import flanagan.complex.Complex;
import flanagan.math.Polynomial;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/** Reconstructs phi from the known (reconstructed) center and the sample finding the root of the polynomial
  * If the center is not specified, uses CenterReconstructor */
public class PolynomialReconstructor implements MallowsReconstructor {

  
  /** Normalization factor for n elements */
  private static Polynomial z(int n) {
    Polynomial z = new Polynomial(1d);
    for (int i = 1; i < n; i++) {
      double[] a = new double[i+1];
      Arrays.fill(a, 1d);
      Polynomial f = new Polynomial(a);
      z = z.times(f);
    }
    return z;
  }
  
  
  /** Polynomial of size n(n-1)/2 with number of rankings at distance d, times d */
  private static Polynomial c(int n) {
    int N = n * (n-1) / 2;
    double[] c = new double[N+1];
    for (int i = 0; i < c.length; i++) {
      c[i] = i * KendallTauUtils.getCount(n, i);
    }
    return new Polynomial(c);
  }
  

  @Override
  public MallowsModel reconstruct(Sample sample) {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }
  
  
  @Override
  public MallowsModel reconstruct(Sample sample, Ranking center) {
    double sumd = 0;
    for (Sample.RW rw: sample.enumerate()) {
      sumd += rw.w * KendallTauDistance.getInstance().distance(center, rw.r);
    }
    double meand = sumd / sample.sumWeights();
    
    int n = sample.getElements().size();
    Polynomial left = z(n).times(meand);
    Polynomial right = c(n);
    
    Polynomial solve = left.minus(right);
    
    double[] a = solve.coefficientsReference();
    
    edu.drexel.cs.db.rank.math.Polynomial solverson = new edu.drexel.cs.db.rank.math.Polynomial(a);
    double phi = solverson.root(0d, 1d, 0.00001d);
    return new MallowsModel(center, phi);
  }
  
  
  
  public static void main(String[] args) {
    int n = 20;    
    ElementSet elements = new ElementSet(n);
    double phi = Math.random() * 0.8;
    Ranking center = elements.getRandomRanking();
    MallowsModel model = new MallowsModel(center, phi);
    
    int sampleSize = 5000;
    MallowsTriangle triangle = new MallowsTriangle(model);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    
    PolynomialReconstructor rec = new PolynomialReconstructor();
    MallowsModel mm = rec.reconstruct(sample, center);
    System.out.println(model);
    System.out.println(mm);
  }

}