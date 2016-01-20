package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;


public class RankingSimilarity {

  public static double similarity(Ranking r1, Ranking r2) {
    int[][] table = table(r1);    
    int sim = 0;
    for (int i = 0; i < r2.size()-1; i++) {
      int e1 = r2.get(i).getId();
      for (int j = i+1; j < r2.size(); j++) {
        int e2 = r2.get(j).getId();
        if (table[e1][e2] > 0) sim++;
      }
    }   
    return sim;
  }
  
  
  public static int[][] table(Ranking r) {
    int n = r.getItemSet().size();
    int[][] table = new int[n][n];
    
    for (int i = 0; i < r.size()-1; i++) {
      int e1 = r.get(i).getId();
      for (int j = i+1; j < r.size(); j++) {
        int e2 = r.get(j).getId();
        table[e1][e2]++;
      }
    }    
    return table;
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    Ranking r1 = items.getRandomRanking(4);
    Ranking r2 = items.getRandomRanking(4);
    System.out.println(r1);
    System.out.println(r2);
    System.out.println(RankingSimilarity.similarity(r1, r2));
  }
}
