package edu.drexel.cs.db.db4pref.math;

import edu.drexel.cs.db.db4pref.util.Logger;


public class Polynomial {

  private double[] a; // a[0] + a[1] * x + a[2] * x^2 + ... + a[n-1] * x^(n-1)
  
  
  public Polynomial(double c) {
    a = new double[1];
    a[0] = c;
  }
  
  public Polynomial(double[] a) {
    this.a = a;
  }
  
  public double eval(double x) {
    double p = 1;
    double s = 0;
    double temp;
    for (int i = 0; i < a.length; i++) {
      temp = s + p * a[i];
      if (Double.isInfinite(temp) || Double.isNaN(temp)) break;
      // s += p * a[i];
      s = temp;
      p *= x;
    }
    return s;
  }
  
  /** Coefficient at degree i */
  public double coef(int i) {
    return a[i];
  }
  

  public double root(double min, double max, double epsilon) {
    double v1 = eval(min);
    double v2 = eval(max);
    return root(min, max, v1, v2, epsilon);
  }
  
  private double root(double min, double max, double v1, double v2, double epsilon) {
    if (Double.isInfinite(v1) || Double.isNaN(v1)) return Double.NaN;
    if (Double.isInfinite(v2) || Double.isNaN(v2)) return Double.NaN;
    double s1 = Math.signum(v1);
    if (s1 == 0) return min;
    double s2 = Math.signum(v2);
    if (s2 == 0) return max;
    if (s1 == s2) return Double.NaN;
    
    double mid = (min + max) / 2;
    if (max - mid < epsilon) return mid;
    
    double v0 = eval(mid);
    if (Math.signum(v1) * Math.signum(v0) < 0) return root(min, mid, v1, v0, epsilon);
    if (Math.signum(v0) * Math.signum(v2) < 0) return root(mid, max, v0, v2, epsilon);    
    return mid;
  }
  
  
  /** Multiply this polynomial with a scalar */
  public Polynomial mul(double mul) {
    double[] b = new double[a.length];
    for (int i = 0; i < b.length; i++) {
      b[i] = mul * a[i];      
    }
    return new Polynomial(b);
  }
  
  /** Multiply this polynomial with -1 */
  public Polynomial neg() {
    return this.mul(-1d);
  }
  
  /** @return Polynomial this + b */
  public Polynomial add(Polynomial b) {
    double[] c = new double[Math.max(a.length, b.a.length)];
    for (int i = 0; i < a.length; i++) c[i] = a[i];
    for (int i = 0; i < b.a.length; i++) c[i] += b.a[i];
    return new Polynomial(trim(c));
  }
  
  /** @return Polynomial this - b */
  public Polynomial sub(Polynomial b) {
    double[] c = new double[Math.max(a.length, b.a.length)];
    for (int i = 0; i < a.length; i++) c[i] = a[i];
    for (int i = 0; i < b.a.length; i++) c[i] -= b.a[i];
    return new Polynomial(trim(c));
  }
  
  
  /** @return Polynomial this * b */
  public Polynomial mul(Polynomial b) {
    double[] c = new double[a.length + b.a.length - 1];
    
    for (int i = 0; i < a.length; i++)
      for (int j = 0; j < b.a.length; j++)
        c[i + j] += (a[i] * b.a[j]);

    return new Polynomial(trim(c));
  }
  
  
  public static double[] trim(double[] a) {
    int len = a.length;
    while (len > 0 && a[len-1] == 0) len--;
    if (len == a.length) return a;

    double[] b = new double[len];
    for (int i = 0; i < b.length; i++) {
      b[i] = a[i];        
    }
    return b;    
  }
  
  public void trim() {
    a = trim(a);
  }
  
  @Override
  public String toString() {    
    StringBuilder sb = new StringBuilder("y = ");
    if (a.length == 0) sb.append("0");
    for (int i = 0; i < a.length; i++) {
      sb.append(a[i]);
      if (i > 0) sb.append("\u00b7x");
      if (i > 1) sb.append("^").append(i);
      if (i < a.length - 1) sb.append(" + ");
    }
    return sb.toString();
  }
  
  
  public static void main(String[] args) {
    double[] a = {-0.52, 2, 3};
    Polynomial p = new Polynomial(a);
    System.out.println(p);
    System.out.println(p.root(0, 1, 0.0001)); 
    System.out.println(p.mul(2));
    System.out.println(p.sub(p));
    
    System.out.println(p.sub(p));
    System.out.println(p.mul(p.mul(p)));
    
  }
  
}
