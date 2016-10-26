package edu.drexel.cs.db.db4pref.util;

import cern.colt.Arrays;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.DoubleStream;

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
  
  /** Number of combinations to put n balls into m buckets */
  public static BigInteger ballsInBuckets(int balls, int buckets) {
    return MathUtils.factorial(balls + buckets - 1).divide(MathUtils.factorial(balls)).divide(MathUtils.factorial(buckets - 1));
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
  
  public static double median(double[] doubleList) {
    java.util.Arrays.sort(doubleList);
    int length = doubleList.length;
    if (length % 2 == 0) return doubleList[length / 2];
    else return doubleList[(length - 1) / 2];
  }
  
  public static double[] concat(double[] a, double[] b) {
    double[] c = new double[a.length + b.length];
    for (int i = 0; i < a.length; i++) {
      c[i] = a[i];
    }
    for (int i = 0; i < b.length; i++) {
      c[i + a.length] = b[i];
    }
    return c;
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
    // double suma = 0; 
    // for (int i = 0; i < a.length; i++) suma += a[i];
    double suma = DoubleStream.of(a).sum();
    if (suma == 0) return;
    double c = sum / suma;
    for (int i = 0; i < a.length; i++) {
      a[i] *= c;
    }
  }
    
  public static String toString(double[][] a) {
    StringBuilder sb = new StringBuilder();
    for (double[] row: a) {
      sb.append(Arrays.toString(row)).append('\n');
    }
    return sb.toString();
  }
  
  public static String toString(int[][] a) {
    StringBuilder sb = new StringBuilder();
    for (int[] row: a) {
      sb.append(Arrays.toString(row)).append('\n');
    }
    return sb.toString();
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
