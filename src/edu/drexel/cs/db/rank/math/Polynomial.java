package edu.drexel.cs.db.rank.math;


public class Polynomial {

  private double[] a;
  
  public Polynomial(double[] a) {
    this.a = a;
  }
  
  public double eval(double x) {
    double p = 1;
    double s = 0;
    for (int i = 0; i < a.length; i++) {
      s += p * a[i];
      p *= x;
    }
    return s;
  }
  
  public double root(double min, double max, double epsilon) {
    double v1 = eval(min);
    double v2 = eval(max);
    if (v1 * v2 > 0) return Double.NaN;
    
    double mid = (min + max) / 2;
    if (max - mid < epsilon) return mid;
    
    double v0 = eval(mid);
    if (v1 * v0 < 0) return root(min, mid, epsilon);
    if (v0 * v2 < 0) return root(mid, max, epsilon);    
    return mid;
  }
  
  
  public static void main(String[] args) {
    double[] a = {-0.52, 2, 3};
    Polynomial p = new Polynomial(a);
    System.out.println(p.root(0, 1, 0.0001)); 
  }
  
}
