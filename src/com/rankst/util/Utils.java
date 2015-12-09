package com.rankst.util;

import java.security.SecureRandom;
import java.util.Random;


public class Utils {

  public static final Random random = new SecureRandom();
  
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
  
}
