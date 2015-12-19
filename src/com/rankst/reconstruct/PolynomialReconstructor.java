
package com.rankst.reconstruct;

import com.rankst.comb.Comb;
import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.distance.KendallTauUtils;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.histogram.Histogram;
import com.rankst.kemeny.BubbleTableKemenizator;
import com.rankst.kemeny.Kemenizator;
import com.rankst.model.MallowsModel;
import com.rankst.temp.PolynomialRootFinder;
import com.rankst.triangle.MallowsTriangle;
import flanagan.complex.Complex;
import flanagan.math.Polynomial;
import java.util.Arrays;
import org.ejml.data.Complex64F;

/** Reconstructs Mallows model using Kemenization to get the center, and then finding the root of the polynomial for phi */
public class PolynomialReconstructor implements MallowsReconstructor {

  /** Starting values for solving the polynomial */
  private double[] starts = { 0.5d, 0.875d, 0.125d, 0.75d, 0.375d, 1d, 0.625d, 0.25d, 0d }; // { 0.5d, 1d, 0d, 0.75d, 0.25d };
  
  private long timeCenter = 0;
  private long timePhi = 0;
  private long timeTotal = 0;
  private double start = Double.NaN;


  public long getTimeCenter() {
    return timeCenter;
  }

  public long getTimePhi() {
    return timePhi;
  }

  public long getTimeTotal() {
    return timeTotal;
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
  
  protected Ranking reconstructCenter(Sample sample) {
    Histogram<Ranking> rankHist = new Histogram(sample, sample.getWeights());
    Ranking before = rankHist.getMostFrequent();
    Kemenizator kemenizator = new BubbleTableKemenizator();
    Ranking after = kemenizator.kemenize(sample, before);
    return after;
  }
  
  @Override
  public MallowsModel reconstruct(Sample sample) {
    long t0 = System.currentTimeMillis();
    Ranking center = reconstructCenter(sample);
    this.timeCenter = System.currentTimeMillis() - t0;
    
    long t1 = System.currentTimeMillis();
    double sumd = 0;
    for (Ranking r: sample) sumd += KendallTauRankingDistance.getInstance().distance(center, r);
    double meand = sumd / sample.size();
    
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
    
//    
//    if (Double.isNaN(phi)) {
//      System.err.println("Phi is NaN. Trying the other solver...");
//      
//      Complex64F[] cs = PolynomialRootFinder.findRoots(solve.coefficientsReference());
//      phi = getPhi(cs);
//      LaguerreSolver solver = new LaguerreSolver();
//      double[] coefs = solve.coefficientsReference();
//      System.err.println("Polynomial ^"+coefs.length);
//      org.apache.commons.math3.complex.Complex[] cs = solver.solveAllComplex(solve.coefficientsCopy(), 0.5);
//      System.err.println(cs.length + " roots...");
//      phi = getPhi(cs);
      
      
      
//      for (double s : starts) {
//        Complex[] roots = solve.laguerreAll(s);
//        phi = getPhi(roots);
//        if (!Double.isNaN(phi)) {
//          start = s;
//          break;
//        }
//      }
//      System.err.println("After: phi = " + phi);
//    }
//    
    
    this.timePhi = System.currentTimeMillis() - t1;
    this.timeTotal = System.currentTimeMillis() - t0;
    
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
  
  
  /** Find the best candidate for phi, or return NaN */
  private double getPhi(Complex64F[] roots) {
    double bestRoot = Double.NaN;
    double minImag = Double.POSITIVE_INFINITY;
    
    for (Complex64F root: roots) {
      double imag = root.getImaginary();
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
  
  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(40);
    Ranking reference = elements.getRandomRanking();
    
    double phi = 0.85;
    int sampleSize = 1500;
    
    MallowsTriangle triangle = new MallowsTriangle(reference, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);

    PolynomialReconstructor rec = new PolynomialReconstructor();
    MallowsModel mallows = rec.reconstruct(sample);
    System.out.println(mallows);

    System.out.println(KendallTauRankingDistance.between(reference, mallows.getCenter()));
    
    Comb.comb(sample, 0.1);
    
//    SampleTriangle st = new SampleTriangle(sample);
//    RIMRSampler resampler = new RIMRSampler(st);
//    Sample resample = resampler.generate(10000);
//    System.out.println(rec.reconstruct(resample));
//    
//    SampleTriangleByRow str = new SampleTriangleByRow(sample);
//    RIMRSampler resampler1 = new RIMRSampler(str);
//    Sample resample1 = resampler1.generate(10000);
//    System.out.println(rec.reconstruct(resample1));
    
//    SampleCompleter completer = new SampleCompleter(sample);
//    Sample resample2 = completer.complete(1);    
//    System.out.println(rec.reconstruct(resample2));
  }

}
