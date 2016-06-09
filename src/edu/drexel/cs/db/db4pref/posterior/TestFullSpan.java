package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.Random;


public class TestFullSpan {

  public static void main(String[] args) {
    Random random = new Random();
    int[] its = { 30 };
    double[] phis = { 0.2, 0.5, 0.8 };
    double[] misses = { 0.2, 0.4, 0.6, 0.8 };
    
    for (int i = 0; i < 1000; i++) {
      int m = its[random.nextInt(its.length)];
      ItemSet items = new ItemSet(m);
      
      double miss = misses[random.nextInt(misses.length)];
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      Filter.removeItems(r, miss);
      
      
      // Logger.info("%d out of %d items", r.length(), m);
      
      SpanExpander span = new SpanExpander(model);
      long starts = System.currentTimeMillis();
      double ps = span.getProbability(r);
      long ts = System.currentTimeMillis() - starts;
      // Logger.info("[Span] time %d ms, max states: %d, width: %d", ts, span.getMaxStates(), span.getWidth());
      
      
      FullExpander full = new FullExpander(model);
      long startf = System.currentTimeMillis();
      double pf = full.getProbability(r);
      long tf = System.currentTimeMillis() - startf;
      // Logger.info("[Full] time %d ms, max states: %d", tf, full.getMaxStates());
      
      
      
      Logger.info("%d,%.1f,%d,%f,%d,%d,%f,%d,%d,%d", m, phi, r.length(), pf, tf, full.getMaxStates(), ps, ts, span.getMaxStates(), span.getWidth());
    }
  }
}
