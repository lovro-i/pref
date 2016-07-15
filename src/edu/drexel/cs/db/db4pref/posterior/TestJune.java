package edu.drexel.cs.db.db4pref.posterior;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.filter.MissingProbabilities;
import edu.drexel.cs.db.db4pref.gm.GraphicalModel;
import edu.drexel.cs.db.db4pref.gm.JayesInferator;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.sampler.AMPSampler;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeoutException;


public class TestJune {

  public static void main(String[] args) throws IOException, NoSuchAlgorithmException, TimeoutException {
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
      // three(out);
      // four(out);
      // five(out);
      six(out);
    } 
  }
  
  /** Compare Span and Jayes versions 15x for each random ranking */
  public static void three(PrintWriter out) throws NoSuchAlgorithmException, TimeoutException {
    Random random = new Random();
    int[] its = { 20, 30, 40 };
    double[] phis = { 0.2, 0.5, 0.8 };
    double[] misses = { 0.2, 0.4, 0.6, 0.8 };
    int[] amps = { 100, 1000, 10000 };
    
    for (int i = 0; i < 1000; i++) {
      int m = its[random.nextInt(its.length)];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double miss = misses[random.nextInt(misses.length)];
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniform(items, miss));
      String vName = v.toString();
      vName = vName.replace(" ", "").replace(',', ' ');
      Logger.info("Executing %s [%d]", vName, m);
      
      int tests = 10;
      boolean jayes = true;
      int networkSize = -1;
      double pJayes = -1;
      int jayesJunctionTreeWidth = -1;
      long timeGM = -1;
      long timeJayes = -1;
      long timeGMJayes = -1;
      
      for (int j = 0; j <= tests; j++) {
        
        // EXPANDER
        long starts = System.currentTimeMillis();
        PreferenceExpander expander = new PreferenceExpander(model);
        double pExpander = expander.getProbability(v);
        long timeExpander = System.currentTimeMillis() - starts;


        // JAYES
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
        
        
        // AMP
        AMPSampler ampSampler = new AMPSampler(model);
        double[] b = new double[0];
        double[] pAmps = new double[amps.length];
        long[] timeAmps = new long[amps.length];
        long startAmp = System.currentTimeMillis();
        
        for (int k = 0; k < amps.length; k++) {
          int as = amps[k];
          int reps = as - b.length;
          double[] a = ampSampler.samplePosteriors(v, reps);
          b = MathUtils.concat(b, a);
          pAmps[k] = MathUtils.mean(b);
          timeAmps[k] = System.currentTimeMillis() - startAmp;
        }
        
        
        String line = String.format("%d,%.1f,%s,%d", m, phi, vName, v.size());
        line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
        line += String.format(",%d,%d,%f,%d,%d,%d", networkSize, jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
        for (int k = 0; k < amps.length; k++) {
          line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
        }
        
        
        Logger.info("[%d / %d] %s", j, tests, line);
        
        if (j > 0) {
          out.println(line);
          out.flush();
        }
        
      }
      
    }
  }
  
  /** Compare Span and Jayes versions 15x for each random ranking */
  public static void five(PrintWriter out) throws NoSuchAlgorithmException, TimeoutException {
    Random random = new Random();
    int[] its = {5, 10, 15}; // { 20, 30, 40, 50, 50, 50, 50, 50 }; //,50 , 60, 70, 80, 90, 100 };
    double[] phis = { 0.2, 0.5, 0.8 };
    double[] misses = { 0.2, 0.4, 0.6, 0.8 };
    int[] amps = { 100, 1000, 5000, 10000 };
    
    for (int i = 1; i < 1000; i++) {
      int m = its[random.nextInt(its.length)];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double miss = 0.85 * random.nextDouble();
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniform(items, miss));
      if (v.size() < 2) continue;
      String vName = v.toString();
      vName = vName.replace(" ", "").replace(',', ' ');
      
      
      int tests = 3;
      boolean jayes = true;
      int networkSize = -1;
      double pJayes = -1;
      int jayesJunctionTreeWidth = -1;
      long timeGM = -1;
      long timeJayes = -1;
      long timeGMJayes = -1;
      
      for (int test = 1; test <= tests; test++) {
        Logger.info("Executing test %d, rep %d/%d: %d items, %d pairs", i, test, tests, m, v.size());
        
        // EXPANDER
        long starts = System.currentTimeMillis();
        PreferenceExpander expander = new PreferenceExpander(model);
        double pExpander = expander.getProbability(v);
        long timeExpander = System.currentTimeMillis() - starts;
        Logger.info("Expander done in %d sec", timeExpander / 1000);

        // JAYES
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
            Logger.info("Out of memory for Jayes. Turning off..."); 
            jayes = false;
            pJayes = -1;
            timeJayes = System.currentTimeMillis() - startJayes;
            timeGMJayes = timeGM + timeJayes;
          }
          Logger.info("Jayes done in %d sec", timeGMJayes / 1000);
        }
        
        
        // AMP
        AMPSampler ampSampler = new AMPSampler(model);
        double[] b = new double[0];
        double[] pAmps = new double[amps.length];
        long[] timeAmps = new long[amps.length];
        long startAmp = System.currentTimeMillis();
        
        for (int k = 0; k < amps.length; k++) {
          int as = amps[k];
          int reps = as - b.length;
          double[] a = ampSampler.samplePosteriors(v, reps);
          b = MathUtils.concat(b, a);
          pAmps[k] = MathUtils.mean(b);
          timeAmps[k] = System.currentTimeMillis() - startAmp;
          Logger.info("AMP %d done in %d sec", as, timeAmps[k] / 1000);
        }
        
        
        String line = String.format("%d,%.1f,%s,%d,%d", m, phi, vName, v.size(), test);
        line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
        line += String.format(",%d,%d,%f,%d,%d,%d", networkSize, jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
        for (int k = 0; k < amps.length; k++) {
          line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
        }
        
        out.println(line);
        out.flush();
      }
      
    }
  }
  
  /** Compare Span and Jayes versions 15x for each random ranking */
  public static void six(PrintWriter out) throws NoSuchAlgorithmException, TimeoutException {
    Random random = new Random();
    int[] its = { 10, 20, 30, 40, 50 }; //, 60, 70, 80, 90, 100 };
    double[] phis = { 0.2, 0.5, 0.8 };
    double[] misses = { 0.2, 0.4, 0.6, 0.8 };
    int[] amps = { 100, 500, 1000, 5000, 10000, 50000 };
    
    for (int i = 1; i < 100000; i++) {
      int m = its[random.nextInt(its.length)];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double miss = 0.85 * random.nextDouble();
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      Filter.removeItems(r, miss);
      if (r.length() < 2 || r.length() == m) continue;
      MapPreferenceSet v = r.transitiveClosure();
      String vName = v.toString().replace(" ", "").replace(',', ' ');
      
      
      Logger.info("Executing test %d: %d/%d items", i, r.length(), m);

      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      double pExpander = expander.getProbability(v);
      long timeExpander = System.currentTimeMillis() - starts;
      // Logger.info("Expander done in %d sec", timeExpander / 1000);

      /*
      // JAYES
      long startGM = System.currentTimeMillis();
      long startJayes = 0;
      int networkSize = -1;
      double pJayes = -1;
      int jayesJunctionTreeWidth = -1;
      long timeGM = -1;
      long timeJayes = -1;
      long timeGMJayes = -1;

      try {
        // GM Common
        GraphicalModel gm = new GraphicalModel(model, v);
        gm.build();
        networkSize = gm.getNetworkSize();
        timeGM = System.currentTimeMillis() - startGM;
        
        // JAYES
        startJayes = System.currentTimeMillis();
        JayesInferator jayesInferator = new JayesInferator(gm);
        System.out.println("aaaaacc");
        pJayes = jayesInferator.getProbability();
        System.out.println("aaaaab");

        jayesJunctionTreeWidth = jayesInferator.getJunctionTreeWidth();
        timeJayes = System.currentTimeMillis() - startJayes;
        timeGMJayes = timeGM + timeJayes;
      }
      catch (OutOfMemoryError e) {
        Logger.info("Out of memory for Jayes. Turning off..."); 
        pJayes = -1;
        timeJayes = System.currentTimeMillis() - startJayes;
        timeGMJayes = timeGM + timeJayes;
      }
      Logger.info("Jayes done in %d sec", timeGMJayes / 1000);
      */

      // AMP
      AMPSampler ampSampler = new AMPSampler(model);
      double[] b = new double[0];
      double[] pAmps = new double[amps.length];
      long[] timeAmps = new long[amps.length];
      long startAmp = System.currentTimeMillis();

      for (int k = 0; k < amps.length; k++) {
        int as = amps[k];
        int reps = as - b.length;
        double[] a = ampSampler.samplePosteriors(v, reps);
        b = MathUtils.concat(b, a);
        pAmps[k] = MathUtils.mean(b);
        timeAmps[k] = System.currentTimeMillis() - startAmp;
        // Logger.info("AMP %d done in %d sec", as, timeAmps[k] / 1000);
      }


      String line = String.format("%d,%.1f,%s,%d,%d", m, phi, vName, r.length(), v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      // line += String.format(",%d,%d,%f,%d,%d,%d", networkSize, jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
      for (int k = 0; k < amps.length; k++) {
        line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
      }

      out.println(line);
      out.flush();
      
    }
  }
  
  /** Compare Span and Jayes versions 15x for each random ranking */
  public static void four(PrintWriter out) throws TimeoutException {
    Random random = new Random();
    int[] its = { 70, 80, 90, 100 };
    double[] phis = { 0.2, 0.5, 0.8 };
    double[] misses = { 0.2, 0.4, 0.6, 0.8 };
    
    for (int i = 1; i <= 100000; i++) {
      int m = its[random.nextInt(its.length)];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double miss = 0.85 * random.nextDouble(); // misses[random.nextInt(misses.length)];
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniform(items, miss));
      String vName = v.toString();
      vName = vName.replace(" ", "").replace(',', ' ');
      Logger.info("Executing test %d: %d items, %d pairs", i, m, v.size());      
        
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      double pExpander = expander.getProbability(v);
      long timeExpander = System.currentTimeMillis() - starts;


      String line = String.format("%d,%.1f,%s,%d", m, phi, vName, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());

      Logger.info("Done in %d sec", timeExpander / 1000);

      out.println(line);
      out.flush();
        
    }
  }
  
  
}
