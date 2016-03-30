package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.math.Polynomial;
import edu.drexel.cs.db.rank.reconstruct.PolynomialReconstructor;
import java.util.HashMap;
import java.util.Map;

/** Maximum n value for which you don't get Infinity anywhere is 171 */
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
    int n = 171;    
    System.out.println(getCount(n, n * (n - 1) / 4));    
//    for (int i = 0; i <= n * (n - 1) / 2; i++) {
//      Logger.info("%d: %f", i, getCount(n, i));      
//    }
  }
  
}
