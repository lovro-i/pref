
package com.rankst.test;

import flanagan.complex.Complex;
import flanagan.math.Polynomial;


public class TestPolynomial {

  public static void main(String[] args) {
    // 0.872 = 3 * x + 5 * x^2 + 9 * x^3 (x = 0.2)
    
    double[] a = new double[4];
    a[0] = -0.872;
    a[1] = 3;
    a[2] = 5;
    a[3] = 9;
    
    Polynomial p = new Polynomial(a);
    Complex[] roots = p.roots(0.5);
    System.out.println("Roots: " + roots.length);
    for (Complex root: roots) System.out.println(root.getReal());
  }
}
