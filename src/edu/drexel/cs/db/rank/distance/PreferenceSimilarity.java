package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;


public class PreferenceSimilarity {

  public static double similarity(PreferenceSet pref1, PreferenceSet pref2) {
    ItemSet items = pref1.getItemSet();
    int n = items.size();
    int similarity = 0;
    for (int i = 0; i < n-1; i++) {
      for (int j = i+1; j < n; j++) {
        Boolean b1 = pref1.isHigher(i, j);
        if (b1 != null) {
          Boolean b2 = pref2.isHigher(i, j);
          if (b2 != null && b1.equals(b2)) {
            similarity++;
          }
        }
      }      
    }
    return similarity;
  }
  
  
//  public static double similarity(Ranking r1, Ranking r2) {
//    int[][] table = table(r1);    
//    int sim = 0;
//    for (int i = 0; i < r2.size()-1; i++) {
//      int e1 = r2.get(i).getId();
//      for (int j = i+1; j < r2.size(); j++) {
//        int e2 = r2.get(j).getId();
//        if (table[e1][e2] > 0) sim++;
//      }
//    }   
//    return sim;
//  }
//  
//  
//  public static int[][] table(Ranking r) {
//    int n = r.getItemSet().size();
//    int[][] table = new int[n][n];
//    
//    for (int i = 0; i < r.size()-1; i++) {
//      int e1 = r.get(i).getId();
//      for (int j = i+1; j < r.size(); j++) {
//        int e2 = r.get(j).getId();
//        table[e1][e2]++;
//      }
//    }    
//    return table;
//  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    Ranking r1 = items.getRandomRanking(4);
    Ranking r2 = items.getRandomRanking(4);
    System.out.println(r1);
    System.out.println(r2);
    System.out.println(PreferenceSimilarity.similarity(r1, r2));
    System.out.println(PreferenceSimilarity.similarity(r1.transitiveClosure(), r2.transitiveClosure()));
  }
}
