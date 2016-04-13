package edu.drexel.cs.db.rank.util;

import cern.colt.Arrays;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class MathUtils {

  public static final Random RANDOM = new SecureRandom();
  
  public static BigInteger choose(final int n, final int k) {
    BigInteger ret = BigInteger.ONE;
    for (int i = 0; i < k; i++) {
      ret = ret.multiply(BigInteger.valueOf(n - i)).divide(BigInteger.valueOf(i + 1));
    }
    return ret;
  }
  

  public static BigInteger factorial(final int n) {
    BigInteger p = BigInteger.ONE;
    for (int i = 2; i <= n; i++) {
      p = p.multiply(BigInteger.valueOf(i));      
    }
    return p;
  }
  
  private static long mixes(int fixed, int missing) {
    long p = 1;
    for (int i = 1; i <= missing; i++) {
      p *= fixed + i;
    }
    return p;
  }
  
  public static double variance(double[] a) {
    double sum = 0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i];      
    }
    double mean = sum / a.length;
    double var = 0;
    for (int i = 0; i < a.length; i++) {
      double d = a[i] - mean;
      var += d * d;
    }
    var = var / (a.length - 1);
    return var;
  }
  
  public static double mean(double[] a) {    
    return sum(a) / a.length;
  }
  
  public static double sum(double[] a) {
    double s = 0;
    for (int i = 0; i < a.length; i++) {
      s += a[i];      
    }
    return s;
  }
  
  public static double min(double[] a) {
    double min = a[0];
    for (int i = 1; i < a.length; i++) {
      min = Math.min(min, a[i]);
    }
    return min;
  }
  
  public static void normalize(double[] a, double sum) {
    double c = sum / DoubleStream.of(a).sum();
    for (int i = 0; i < a.length; i++) {
      a[i] *= c;
    }
  }
    
    
  public static void main(String[] args) {
    double a[] = { 0, 0, 0, 50, 60, 10, 80 };
    normalize(a, 1d);
    System.out.println(Arrays.toString(a));
    
    
    int n = 220;
    int k = 16;
    BigInteger cbi = choose(n, k);
    System.out.println(cbi);
    System.out.println(String.format("%f", cbi.doubleValue()));
    System.out.println(mixes(1, 1));
    System.out.println(factorial(6));
  }

  
}
