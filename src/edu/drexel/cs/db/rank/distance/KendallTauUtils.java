package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.math.Polynomial;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.HashMap;
import java.util.Map;


public class KendallTauUtils {
  
  
  private static Map<Integer, Polynomial> map = new HashMap<Integer, Polynomial>();
  
  public static synchronized double getCount(int n, int d) {
    Polynomial z = map.get(n);
    if (z == null) {
      z = PolynomialReconstructor.z(n);
      map.put(n, z);
    }
    return z.coef(d);
  }
  
  public static void main(String[] args) {
    int n = 5;
    for (int i = 0; i <= n * (n - 1) / 2; i++) {
      Logger.info("%d: %f", i, getCount(n, i));      
    }
  }
  
}
