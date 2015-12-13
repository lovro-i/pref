package com.rankst.ml;

import java.util.ArrayList;
import java.util.List;

public class TrainUtils {
  
  /** Return array containing values between zero and one with given step */
  public static double[] step(double step, boolean zero) {
    List<Double> ps = new ArrayList<Double>();
    double p = zero ? 0 : step;
    while (p < 1) {
      ps.add(p);
      p += step;
    }
    
    double phis[] = new double[ps.size()];
    for (int i = 0; i < phis.length; i++) {
      phis[i] = ps.get(i);
    }
    
    return phis;
  }
  
  public static double[] step(double start, double end, double step) {
    List<Double> ps = new ArrayList<Double>();
    double p = start;
    while (p <= end + 0.001) {
      ps.add(p);
      p += step;
    }
    
    double phis[] = new double[ps.size()];
    for (int i = 0; i < phis.length; i++) {
      phis[i] = ps.get(i);
    }
    
    return phis;
  }
  
}
