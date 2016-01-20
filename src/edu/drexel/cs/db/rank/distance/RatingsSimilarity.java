package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.rating.Ratings;
import java.util.List;


public class RatingsSimilarity {

  public static double similarity(Ratings r1, Ratings r2) {
    int[][] table = table(r1);    
    int similarity = 0;
    
    List<List<Item>> groups = r2.getGroups();
    for (int i = 0; i < groups.size()-1; i++) {
      for (Item e1: groups.get(i)) {
        int ie1 = e1.getId();
        for (int j = i+1; j < groups.size(); j++) {
          for (Item e2: groups.get(j)) {
            int ie2 = e2.getId();
            if (table[ie1][ie2] > 0) similarity++;
          }
        }
      }
    } 
    
    return similarity;
  }
  
  
  public static int[][] table(Ratings r) {
    List<List<Item>> groups = r.getGroups();
    int n = r.getItemSet().size();
    int[][] table = new int[n][n];
    
    for (int i = 0; i < groups.size()-1; i++) {
      for (Item e1: groups.get(i)) {
        int ie1 = e1.getId();
        for (int j = i+1; j < groups.size(); j++) {
          for (Item e2: groups.get(j)) {
            int ie2 = e2.getId();
            table[ie1][ie2]++;
          }
        }
      }
    }    
    return table;
  }
  
}
