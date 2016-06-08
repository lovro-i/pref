package edu.drexel.cs.db.db4pref.distance;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.util.Polynomial;
import edu.drexel.cs.db.db4pref.reconstruct.PolynomialReconstructor;
import java.util.HashMap;
import java.util.Map;


/** Simple Kendall tau distance between the intersection of two rankings */
public class KendallTauDistance implements RankingDistance {
  
  private static KendallTauDistance distance = new KendallTauDistance();
  
  private static Map<Integer, Polynomial> polynomials = new HashMap<Integer, Polynomial>();
  
  public KendallTauDistance() {    
  }
  
  public static KendallTauDistance getInstance() {
    return distance;
  }
  
  
  /** Returns the number of rankings of length n at distance d.
   * Maximum n value for which you don't get Infinity anywhere is 170 */
  public static synchronized double getCount(int n, int d) {
    Polynomial z = polynomials.get(n);
    if (z == null) {
      z = PolynomialReconstructor.z(n);
      polynomials.put(n, z);
    }
    return z.coef(d);
  }
  
  public static double between(Ranking ranking1, Ranking ranking2) {
    return distance.distance(ranking1, ranking2);
  }
  
  public double distance(PreferenceSet pref1, PreferenceSet pref2) {
    ItemSet items = pref1.getItemSet();
    double distance = 0;
    for (int i = 0; i < items.size()-1; i++) {
      Item it1 = items.get(i);
      for (int j = i+1; j < items.size(); j++) {
        Item it2 = items.get(j);
        Boolean b1 = pref1.isPreferred(it1, it2);
        Boolean b2 = pref2.isPreferred(it1, it2);
        if (b1 != null && b2 != null && !b1.equals(b2)) distance++;
      }
      
    }
    return distance;
  }
  
  @Override
  public double distance(Ranking r1, Ranking r2) {
    return distance(r1.transitiveClosure(), r2.transitiveClosure());
  }
  

  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    
    Ranking r1 = new Ranking(items);
    r1.add(items.getItemById(0));
    r1.add(items.getItemById(3));
    r1.add(items.getItemById(4));
    r1.add(items.getItemById(1));
    r1.add(items.getItemById(2));
    
    r1.add(items.getItemById(5));
    
//    Ranking r2 = new Ranking(r1);
//    r2.randomize();
    Ranking r2 = items.getReferenceRanking();
    //Ranking r2 = new Ranking(items);
//    r2.add(items.getItemById(5));
//    r2.add(items.getItemById(6));
//    r2.add(items.getItemById(7));
//    r2.add(items.getItemById(8));
//    r2.add(items.getItemById(1));
//    r2.add(items.getItemById(4));
//    r2.add(items.getItemById(5));
    
    System.out.println("Distance between");
    System.out.println(r1);
    System.out.println(r2);
    KendallTauDistance dist = new KendallTauDistance();
    System.out.println(dist.distance(r1, r2));
  }
}
