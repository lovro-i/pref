
package com.rankst.reconstruct;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.distance.KendallTauUtils;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.model.MallowsModel;
import flanagan.complex.Complex;
import flanagan.math.Polynomial;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/** Reconstructs phi from the known (reconstructed) center and the sample finding the root of the polynomial
  * If the center is not specified, uses CenterReconstructor */
public class PolynomialReconstructor implements MallowsReconstructor {

  /** Starting values for solving the polynomial */
  private double[] starts = { 0.5d, 0.875d, 0.125d, 0.75d, 0.375d, 1d, 0.625d, 0.25d, 0d }; // { 0.5d, 1d, 0d, 0.75d, 0.25d };
  
  private long time = 0;
  private double start = Double.NaN;


  public long getTime() {
    return time;
  }
  
  
  /** Value from which the polynomial solver started */
  public double getSolverStart() {
    return start;
  }
  
  
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
    long t0 = System.currentTimeMillis();

    double sumd = 0;
    for (Sample.RW rw: sample.enumerate()) {
      sumd += rw.w * KendallTauRankingDistance.getInstance().distance(center, rw.r);
    }
    double meand = sumd / sample.sumWeights();
    
    int n = sample.getElements().size();
    Polynomial left = z(n).times(meand);
    Polynomial right = c(n);
    
    Polynomial solve = left.minus(right);
    
    double phi = Double.NaN;
    this.start = Double.NaN;    
    for (double s: starts) {
      Complex[] roots = solve.roots(s);
      phi = getPhi(roots);
      if (!Double.isNaN(phi)) {
        start = s;
        break;
      }
    }
    
    this.time = System.currentTimeMillis() - t0;    
    return new MallowsModel(center, phi);
  }
  
  
  /** Find the best candidate for phi, or return NaN */
  private double getPhi(Complex[] roots) {
    double bestRoot = Double.NaN;
    double minImag = Double.POSITIVE_INFINITY;
    
    for (Complex root: roots) {
      double imag = root.getImag();
      if (imag == Double.NaN) continue;
      
      double imagAbs = Math.abs(imag);
      double real = root.getReal();
      if (imagAbs < minImag && real >= 0 && real <= 1) {
        bestRoot = real;
        minImag = imagAbs;
      }
    }
    
    double phi = (minImag < 0.01) ? bestRoot : Double.NaN;
    return phi;
  }
   

}
