
package com.rankst.reconstruct;

import com.rankst.comb.Comb;
import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.distance.KendallTauUtils;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.histogram.Histogram;
import com.rankst.kemeny.Kemenizator;
import com.rankst.model.MallowsModel;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.triangle.SampleTriangle;
import com.rankst.triangle.SampleTriangleByRow;
import flanagan.complex.Complex;
import flanagan.math.Polynomial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Reconstructs Mallows model using Kemenization to get the center, and then finding the root of the polynomial for phi */
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
  
  protected Ranking reconstructCenter(Sample sample) {
    Histogram<Ranking> rankHist = new Histogram(sample, sample.getWeights());
    Ranking before = rankHist.getMostFrequent();
    Kemenizator kemenizator = new Kemenizator();
    Ranking after = kemenizator.kemenize(sample, before);
    return after;
  }
  
  @Override
  public MallowsModel reconstruct(Sample sample) {
    Ranking center = reconstructCenter(sample);
    double sumd = 0;
    for (Ranking r: sample) sumd += KendallTauRankingDistance.getInstance().distance(center, r);
    double meand = sumd / sample.size();
    
    int n = sample.getElements().size();
    Polynomial left = z(n).times(meand);
    Polynomial right = c(n);
    
    Polynomial solve = left.minus(right);
    Complex[] roots = solve.roots(0.5d);
    
    double sumRoots = 0;
    int countRoots = 0;
    for (Complex root: roots) {
      if (Math.abs(root.getImag()) > 0.001) continue;      
      double real = root.getReal();
      if (real >= 0 && real <= 1) {
        sumRoots += real;
        countRoots++;
      }
    }
    
    double phi = sumRoots / countRoots;
    return new MallowsModel(center, phi);
  }
  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(10);
    Ranking reference = elements.getReferenceRanking();
    
    double phi = 0.3;
    int sampleSize = 500;
    
    MallowsTriangle triangle = new MallowsTriangle(reference, phi);
    RIMRSampler sampler = new RIMRSampler(triangle);
    Sample sample = sampler.generate(sampleSize);
    
    PolynomialReconstructor rec = new PolynomialReconstructor();
    System.out.println(rec.reconstruct(sample));
    

    Comb.comb(sample, 0.3);
    SampleTriangle st = new SampleTriangle(sample);
    RIMRSampler resampler = new RIMRSampler(st);
    Sample resample = resampler.generate(10000);
    System.out.println(rec.reconstruct(resample));
  }

}
