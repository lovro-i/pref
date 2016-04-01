package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.List;


/** Simple Kendall tau distance between the intersection of two rankings */
public class KendallTauDistance implements RankingDistance {
  
  private static KendallTauDistance distance = new KendallTauDistance();
  
  public KendallTauDistance() {    
  }
  
  public static KendallTauDistance getInstance() {
    return distance;
  }
  
  private int threshold = 2;
  
  /** Set threshold for which the kendall tau distance is computed.
   * if (number_of_common_items &lt; threshold) return Double.POSITIVE_INFINITY
   * Default is 2;
   * @param threshold 
   */
  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public static double between(Ranking ranking1, Ranking ranking2) {
    return distance.distance(ranking1, ranking2);
  }
  
  public double distance(PreferenceSet pref1, PreferenceSet pref2) {
//    ItemSet items = pref1.getItemSet();
//    int n = items.size();
//    int similarity = 0;
//    Set<Integer> set = new HashSet<Integer>();
//    for (int i = 0; i < n-1; i++) {
//      for (int j = i+1; j < n; j++) {
//        Boolean b1 = pref1.isHigher(i, j);
//        Boolean b2 = pref2.isHigher(i, j);
//        if (b1 != null && b2 != null && b1.equals(b2)) {
//          similarity++;
//        }
//        if (b1 != null || b2 != null) {
//          set.add(i);
//          set.add(j);
//        }
//      }      
//    }
//    return set.size() * (set.size() - 1) / 2 - similarity;
    
    
//    ItemSet items = pref1.getItemSet();
//    int n = items.size();
//    int similarity = 0;
//    for (int i = 0; i < n-1; i++) {
//      for (int j = i+1; j < n; j++) {
//        Boolean b1 = pref1.isHigher(i, j);
//        if (b1 != null) {
//          Boolean b2 = pref2.isHigher(i, j);
//          if (b2 != null && b1.equals(b2)) {
//            similarity++;
//          }
//        }
//      }      
//    }
//    return n * (n-1) / 2 - similarity;
//    
//    
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
  

  public double distanceOld(Ranking ranking1, Ranking ranking2) {
    // find intersection
    List<Item> comp1 = new ArrayList<Item>();
    List<Item> comp2 = new ArrayList<Item>();    
    for (int i=0; i<ranking1.length(); i++) {
      Item e1 = ranking1.get(i);
      if (ranking2.contains(e1)) comp1.add(e1);      
    }
    for (int i=0; i<ranking2.length(); i++) {
      Item i2 = ranking2.get(i);
      if (ranking1.contains(i2)) comp2.add(i2);
    }    
    assert(comp1.size() == comp2.size());
    
    // index array
    int common = comp1.size();
    if (common < threshold) return Double.POSITIVE_INFINITY;
    int[] array = new int[common];
    for (int i=0; i<common; i++) {
      Item i2 = comp2.get(i);
      array[i] = comp1.indexOf(i2);
    }
    
    // bubblesort
    long swaps = 0;
    boolean swap = true;
    while (swap) {
      swap = false;
      for (int i=0; i<common-1; i++) {
        if (array[i] > array[i+1]) {
          swaps++;
          swap = true;
          int temp = array[i];
          array[i] = array[i+1];
          array[i+1] = temp;
        }
      }
    }
   
    double distance = 1f * swaps; // / Utils.combinations(common, 2);
    // System.out.println("Common: "+common+", Swaps: "+swaps+", Distance: "+distance);    
    return distance;
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
    System.out.println(dist.distanceOld(r1, r2));
  }
}
