package edu.drexel.cs.db.rank.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

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
    double s = 0;
    for (int i = 0; i < a.length; i++) {
      s += a[i];      
    }
    return s / a.length;
  }
    
    
  public static void main(String[] args) {
    int n = 220;
    int k = 16;
    BigInteger cbi = choose(n, k);
    System.out.println(cbi);
    System.out.println(String.format("%f", cbi.doubleValue()));
    
    System.out.println(mixes(1, 1));
    
    System.out.println(factorial(6));
  }
}
