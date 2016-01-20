package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
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
   * if (number_of_common_items < threshold) return Double.POSITIVE_INFINITY
   * Default is 2;
   * @param threshold 
   */
  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public static double between(Ranking ranking1, Ranking ranking2) {
    return distance.distance(ranking1, ranking2);
  }
  
  @Override
  public double distance(Ranking ranking1, Ranking ranking2) {
    // find intersection
    List<Item> comp1 = new ArrayList<Item>();
    List<Item> comp2 = new ArrayList<Item>();    
    for (int i=0; i<ranking1.size(); i++) {
      Item e1 = ranking1.get(i);
      if (ranking2.contains(e1)) comp1.add(e1);      
    }
    for (int i=0; i<ranking2.size(); i++) {
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
    r1.add(items.getItemById(1));
    r1.add(items.getItemById(2));
    r1.add(items.getItemById(3));
    r1.add(items.getItemById(4));
    r1.add(items.getItemById(5));
    
    Ranking r2 = new Ranking(items);
    r2.add(items.getItemById(0));
    r2.add(items.getItemById(2));
    r2.add(items.getItemById(3));
    r2.add(items.getItemById(1));
    r2.add(items.getItemById(4));
    r2.add(items.getItemById(5));
    
    KendallTauDistance dist = new KendallTauDistance();
    System.out.println(dist.distance(r1, r2));
  }
}
