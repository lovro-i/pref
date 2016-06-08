package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.Sample.PW;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.sampler.FullSample;


public class Test2 {
 
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(6);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    Ranking ref = new Ranking(items);
    ref.add(items.getItemByTag(2));
    ref.add(items.getItemByTag(4));
    
    FullSample full = new FullSample(items);
    double p = 0;
    for (PW<Ranking> pw: full) {
      Ranking r = pw.p;
      if (r.isConsistent(ref)) p += model.getProbability(r);
    }
    System.out.println("Total probability: " + p);
    
    //////// 
    
    
    FullExpander expander = new FullExpander(model);
    
    Ranking r1 = Ranking.fromStringById(items, "1-3-0-5");
    // Ranking r2 = Ranking.fromStringById(items, "1-5-3-0");
    Ranking r3 = Ranking.fromStringById(items, "1-3-5-0");
    Ranking r4 = Ranking.fromStringById(items, "3-0-1-5");
    Ranking r5 = Ranking.fromStringById(items, "3-1-0-5");
    Ranking r6 = Ranking.fromStringById(items, "3-1-5-0");
    
    double p1 = expander.getProbability(r1);
    // double p2 = expander.getProbability(r2);
    double p3 = expander.getProbability(r3);
    double p4 = expander.getProbability(r4);
    double p5 = expander.getProbability(r5);
    double p6 = expander.getProbability(r6);
    
    double pt = p1 + p3 + p4 + p5 + p6;
    System.out.println(pt);
    
  }
}
