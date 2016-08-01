package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.gm.GraphicalModel;
import edu.drexel.cs.db.db4pref.gm.GrmmInferator;
import edu.drexel.cs.db.db4pref.gm.JayesInferator;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeoutException;


public class TestFullSpan {

  public static void main(String[] args) throws IOException, TimeoutException {
    PrintWriter out;
    if (args.length > 0) {
      File file = new File(args[0]);
      Logger.info("Writing to %s", file);
      out = FileUtils.append(file);
    }
    else {
      Logger.warn("No file specified, writing to console");
      out = new PrintWriter(System.out);
    }
    
    while (true) {
      try { three(out); }
      catch (OutOfMemoryError e) { 
        System.out.println("Out of memory... Restarting"); 
        e.printStackTrace();
      }
    } 
  }
  
  /** Compare Span and Jayes versions 15x for each random ranking */
  public static void three(PrintWriter out) throws TimeoutException {
    Random random = new Random();
    int[] its = { 20, 30, 40, 50 };
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
      
      Logger.info("Executing %s [%d / %d]", r, r.length(), m);
      
      
      boolean jayes = true;
      int networkSize = -1;
      double pJayes = -1;
      int jayesJunctionTreeWidth = -1;
      long timeGM = -1;
      long timeJayes = -1;
      long timeGMJayes = -1;
      
      for (int j = 0; j <= 15; j++) {
        // SPAN
        SpanExpander span = new SpanExpander(model);
        long starts = System.currentTimeMillis();
        double pSpan = span.getProbability(r);
        long timeSpan = System.currentTimeMillis() - starts;
        
        long startGM = System.currentTimeMillis();
        long startJayes = 0;

        if (jayes) {
          try {
            // GM Common
            GraphicalModel gm = new GraphicalModel(model, r);
            gm.build();
            networkSize = gm.getNetworkSize();
            timeGM = System.currentTimeMillis() - startGM;

            // JAYES
            startJayes = System.currentTimeMillis();
            JayesInferator jayesInferator = new JayesInferator(gm);
            pJayes = jayesInferator.getProbability();
            jayesJunctionTreeWidth = jayesInferator.getJunctionTreeWidth();
            timeJayes = System.currentTimeMillis() - startJayes;
            timeGMJayes = timeGM + timeJayes;
          }
          catch (OutOfMemoryError e) {
            System.out.println("Out of memory for Jayes. Turning off..."); 
            jayes = false;
            pJayes = -1;
            timeJayes = System.currentTimeMillis() - startJayes;
            timeGMJayes = timeGM + timeJayes;
          }
        }
        
        String line = String.format("%d,%.1f,%s,%d", m, phi, r, r.length());
        line += String.format(",%f,%d,%d,%d,%d", Math.log(pSpan), timeSpan, span.getWidth(), span.getMaxStates(), span.getTotalStates());
        line += String.format(",%d,%d,%f,%d,%d,%d", networkSize, jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
        Logger.info(line);
        
        if (j > 0) {
          out.println(line);
          out.flush();
        }
        
      }
      
    }
  }
  
  /** Compare Full and Span versions 15x for each random ranking */
  public static void two(PrintWriter out) throws TimeoutException {
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
      
      
      for (int j = 0; j <= 15; j++) {
        // SPAN
        SpanExpander span = new SpanExpander(model);
        long starts = System.currentTimeMillis();
        double pSpan = span.getProbability(r);
        long timeSpan = System.currentTimeMillis() - starts;

        // FULL
        FullExpander full = new FullExpander(model);
        long startf = System.currentTimeMillis();
        double pFull = full.getProbability(r);
        long timeFull = System.currentTimeMillis() - startf;

        // GM Common
        long startGM = System.currentTimeMillis();
        GraphicalModel gm = new GraphicalModel(model, r);
        gm.build();
        int networkSize = gm.getNetworkSize();
        long timeGM = System.currentTimeMillis() - startGM;
        
        // GRMM
        long startGrmm = System.currentTimeMillis();
        GrmmInferator grmmInferator = new GrmmInferator(gm);
        grmmInferator.build();
        double pGrmm = grmmInferator.getProbability();
        long timeGrmm = System.currentTimeMillis() - startGrmm + timeGM;
        
        // JAYES
        long startJayes = System.currentTimeMillis();
        JayesInferator jayesInferator = new JayesInferator(gm);
        double pJayes = jayesInferator.getProbability();
        int jayesJunctionTreeWidth = jayesInferator.getJunctionTreeWidth();
        long timeJayes = System.currentTimeMillis() - startJayes + timeGM;
      
        
        String line = String.format("%d,%.1f,%s,%d,%f,%d,%d,%f,%d,%d,%d", m, phi, r, r.length(), Math.log(pFull), timeFull, full.getMaxStates(), Math.log(pSpan), timeSpan, span.getMaxStates(), span.getWidth());
        line += String.format(",%d,%f,%d,%d,%f,%d", networkSize, Math.log(pGrmm), timeGrmm, jayesJunctionTreeWidth, Math.log(pJayes), timeJayes);
        System.out.println(line);
        
        if (j > 0) {
          out.println(line);
          out.flush();
        }
        
      }
      
    }
  }
  
  /** Compare Full and Span versions for a random ranking */
  public static void one() throws TimeoutException {
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
