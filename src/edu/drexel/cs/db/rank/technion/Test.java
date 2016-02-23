package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.util.Logger;


public class Test {

  public static void realTest() {
    ItemSet items = new ItemSet(6);
    items.letters();
    double phi = 0.2;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    
    Ranking r = new Ranking(items);
    r.add(items.getItemById(4));
    r.add(items.getItemById(1));
    r.add(items.getItemById(2));
    System.out.println(r);
    
    Expander expander = new Expander(model, r);
    
    
    {
      Sequence seq = new Sequence(items);
      seq.put(items.get(4), 0);
      seq.put(items.get(1), 2);
      seq.put(items.get(2), 3);      
      System.out.println(seq);    
      double p = Math.pow(phi, 11) + 2 * Math.pow(phi, 10) + Math.pow(phi, 9) + Math.pow(phi, 5) + Math.pow(phi, 4);
      double t = expander.getProbability(seq);
      Logger.info("%.6f %.6f", p, t);
    }
    
    {
      Sequence seq = new Sequence(items);
      seq.put(items.get(1), 2);
      seq.put(items.get(2), 4);
      seq.put(items.get(4), 0);
      System.out.println(seq);    
      double p = Math.pow(phi, 12) + Math.pow(phi, 11) + Math.pow(phi, 9) + Math.pow(phi, 8) + Math.pow(phi, 6) + Math.pow(phi, 5);
      double t = expander.getProbability(seq);
      Logger.info("%.6f %.6f", p, t);
    }
    
    {
      Sequence seq = new Sequence(items);
      seq.put(items.get(1), 2);
      seq.put(items.get(2), 5);
      seq.put(items.get(4), 0);
      System.out.println(seq);    
      double p = Math.pow(phi, 11) + 2 * Math.pow(phi, 10) + Math.pow(phi, 9) + Math.pow(phi, 7) + Math.pow(phi, 6);
      double t = expander.getProbability(seq);
      Logger.info("%.6f %.6f", p, t);
    }
  }
  
  public static void test2() {
    ItemSet items = new ItemSet(3);
    items.letters();
    double phi = 0.2;
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
    
    Ranking r = new Ranking(items);
    r.add(items.getItemById(0));
    r.add(items.getItemById(1));
    System.out.println(r);
    
    Expander expander = new Expander(model, r);
  }
  
  public static void main(String[] args) {
    realTest();
    // test2();
  }
}
