package com.rankst.util;

import java.math.BigInteger;

public class MathUtils {

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
