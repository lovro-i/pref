package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Ratings;
import java.util.List;


public class RatingsSimilarity {

  public static double similarity(Ratings r1, Ratings r2) {
    int[][] table = table(r1);    
    int similarity = 0;
    
    List<List<Element>> groups = r2.getGroups();
    for (int i = 0; i < groups.size()-1; i++) {
      for (Element e1: groups.get(i)) {
        int ie1 = e1.getId();
        for (int j = i+1; j < groups.size(); j++) {
          for (Element e2: groups.get(j)) {
            int ie2 = e2.getId();
            if (table[ie1][ie2] > 0) similarity++;
          }
        }
      }
    } 
    
    return similarity;
  }
  
  
  public static int[][] table(Ratings r) {
    List<List<Element>> groups = r.getGroups();
    int n = r.getElements().size();
    int[][] table = new int[n][n];
    
    for (int i = 0; i < groups.size()-1; i++) {
      for (Element e1: groups.get(i)) {
        int ie1 = e1.getId();
        for (int j = i+1; j < groups.size(); j++) {
          for (Element e2: groups.get(j)) {
            int ie2 = e2.getId();
            table[ie1][ie2]++;
          }
        }
      }
    }    
    return table;
  }
  
}
