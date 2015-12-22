package com.rankst.distance;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;


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
    int n = r.getElementSet().size();
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
    ElementSet elements = new ElementSet(5);
    Ranking r1 = elements.getRandomRanking(4);
    Ranking r2 = elements.getRandomRanking(4);
    System.out.println(r1);
    System.out.println(r2);
    System.out.println(RankingSimilarity.similarity(r1, r2));
  }
}
