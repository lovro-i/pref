package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.util.Logger;


public class Test {

  public static void realTest() {
    ElementSet elements = new ElementSet(6);
    elements.letters();
    double phi = 0.2;
    MallowsModel model = new MallowsModel(elements.getReferenceRanking(), phi);
    
    Ranking r = new Ranking(elements);
    r.add(elements.getElement(4));
    r.add(elements.getElement(1));
    r.add(elements.getElement(2));
    System.out.println(r);
    
    Expander expander = new Expander(model, r);
    
    
    {
      Sequence seq = new Sequence(elements);
      seq.put(elements.get(1), 2);
      seq.put(elements.get(2), 3);
      seq.put(elements.get(4), 0);
      System.out.println(seq);    
      double p = Math.pow(phi, 11) + 2 * Math.pow(phi, 10) + Math.pow(phi, 9) + Math.pow(phi, 5) + Math.pow(phi, 4);
      double t = expander.getProbability(seq);
      Logger.info("%.6f %.6f", p, t);
    }
    
    {
      Sequence seq = new Sequence(elements);
      seq.put(elements.get(1), 2);
      seq.put(elements.get(2), 4);
      seq.put(elements.get(4), 0);
      System.out.println(seq);    
      double p = Math.pow(phi, 12) + Math.pow(phi, 11) + Math.pow(phi, 9) + Math.pow(phi, 8) + Math.pow(phi, 6) + Math.pow(phi, 5);
      double t = expander.getProbability(seq);
      Logger.info("%.6f %.6f", p, t);
    }
    
    {
      Sequence seq = new Sequence(elements);
      seq.put(elements.get(1), 2);
      seq.put(elements.get(2), 5);
      seq.put(elements.get(4), 0);
      System.out.println(seq);    
      double p = Math.pow(phi, 11) + 2 * Math.pow(phi, 10) + Math.pow(phi, 9) + Math.pow(phi, 7) + Math.pow(phi, 6);
      double t = expander.getProbability(seq);
      Logger.info("%.6f %.6f", p, t);
    }
  }
  
  public static void test2() {
    ElementSet elements = new ElementSet(3);
    elements.letters();
    double phi = 0.2;
    MallowsModel model = new MallowsModel(elements.getReferenceRanking(), phi);
    
    Ranking r = new Ranking(elements);
    r.add(elements.getElement(0));
    //r.add(elements.getElement(2));
    r.add(elements.getElement(1));
    System.out.println(r);
    
    Expander expander = new Expander(model, r);
  }
  
  public static void main(String[] args) {
    realTest();
    // test2();
  }
}
