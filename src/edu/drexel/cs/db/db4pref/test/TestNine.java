package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.filter.MissingProbabilities;
import edu.drexel.cs.db.db4pref.gm.GraphicalModel;
import edu.drexel.cs.db.db4pref.gm.JayesInferator;
import edu.drexel.cs.db.db4pref.gm.SampleSearchInferator;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.AMPInferator;
import edu.drexel.cs.db.db4pref.posterior.DynamicPreferenceExpander;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import edu.drexel.cs.db.db4pref.sampler.AMPSampler;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import edu.drexel.cs.db.db4pref.util.TestUtils;
import edu.drexel.cs.db.db4pref.util.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeoutException;


public class TestNine {

  public static void main(String[] args) throws IOException, InterruptedException {
    PrintWriter out = TestUtils.getWriter(args);
    // one(out);
    // two(out);
    // four(out);
    // five(out);
    six(out);
  }
  

  /** Random preference pairs */
  public static void one(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    // int[] its = { 10, 15, 20, 25, 30 };
    double[] phis = { 0.2, 0.5, 0.8 };
    int[] amps = { 100, 1000, 10000 };
    double[] misses = { 0.5, 0.8, 0.2 };
    
    int its = 10;
    int imiss = 0;
    
    for (int i = 0; i < 100000; i++) {
      int m = its;
      its++;
      double miss = misses[imiss];
      double phi = phis[random.nextInt(phis.length)];
      
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
//      Ranking r = new Ranking(items);
//      r.add(items.get(1));
//      r.add(items.get(items.size() - 2));
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      
      Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
      String vName = v.toString();
            
      Logger.info("Executing test %d: %d items (%d of %d pairs), phi = %.1f, miss = %.1f", i+1, m, v.size(), r.size(), phi, miss);

      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      expander.setTimeout(20 * Utils.ONE_MINUTE);
      Double pExpander;
      boolean next = false;
      try { 
        pExpander = expander.getProbability(v);
        if (pExpander.isInfinite()) next = true;
      }
      catch (TimeoutException te) { 
        Logger.info(te.getMessage());
        pExpander = -1d;
        next = true;        
      }
      if (next) {
        its = 10;
        imiss = (imiss + 1) % misses.length;
        continue;
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec: %f", timeExpander / 1000, Math.log(pExpander));
            
      
      String line = String.format("%d,%.1f,%.1f,%s,%d", m, phi, miss, vName, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      

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
        Logger.info("%d AMPs done in %d sec: %f", as, timeAmps[k] / 1000, Math.log(pAmps[k]));
        line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
      }
      

      // SampleSearch
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      
      line += String.format(",%d,%d", timeGM, gm.getNetworkSize());
      
      SampleSearchInferator ss = new SampleSearchInferator(gm);
      for (long time: timeAmps) {
        int t = (int) Math.round(1d * time / 1000);
        t = Math.max(t, 1);
        Double pss = ss.exec(t);
        long timeSS = ss.getTime();
        Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, t, pss);
        line += String.format(",%d,%f,%d", t, pss, timeSS);
      }
      
        
      
      out.println(line);
      out.flush();
      
    }
    out.close();
  }
  
  /** With only one preference pair */
  public static void two(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    int[] its = { 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100 };
    double[] phis = { 0.2, 0.5, 0.8 };
    int[] amps = { 100, 1000, 10000 };
    
    int itIndex = 0;
    
    for (int i = 0; i < 100000; i++) {
      int m = its[itIndex];
      itIndex = (itIndex + 1) % its.length;
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = new Ranking(items);
      r.add(items.get(1));
      r.add(items.get(items.size() - 2));
      MapPreferenceSet v = r.transitiveClosure();
      String vName = v.toString().replace(" ", "").replace(',', ' ');
            
      Logger.info("Executing test %d: %d items (%d of %d pairs), phi = %.1f %s", i+1, m, v.size(), r.size(), phi, vName);

      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      Double pExpander;
      try { 
        pExpander = expander.getProbability(v); 
      }
      catch (TimeoutException te) { 
        Logger.info(te.getMessage());
        itIndex = 0;
        pExpander = -1d; 
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec: %f", timeExpander / 1000, Math.log(pExpander));
            
      
      String line = String.format("%d,%.1f,%s,%d", m, phi, vName, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      

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
        Logger.info("%d AMPs done in %d sec: %f", as, timeAmps[k] / 1000, Math.log(pAmps[k]));
        line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
      }
      

      // SampleSearch
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      
      line += String.format(",%d,%d", timeGM, gm.getNetworkSize());
      
      int[] is = { 2, 6, 10 };
      int t = 1;      
      SampleSearchInferator ss = new SampleSearchInferator(gm);
      for (int iparam : is) {
        ss.setI(iparam);
        Double pss = ss.exec(t);
        long timeSS = ss.getTime();
        Logger.info("SampleSearch done in %d sec: %f", timeSS / 1000, pss);
        line += String.format(",%d,%f,%d", iparam, pss, timeSS);
      }
      
        
      
      out.println(line);
      out.flush();
      
    }
    out.close();
  }
  
  
  /** Random preference pairs */
  public static void four(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    // int[] its = { 10, 15, 20, 25, 30 };
    double[] phis = { 0.5 }; // 0.8
    double[] misses = { 0.2, 0.5, 0.8 };
    
    int its = 10;
    int imiss = 0;
    
    for (int i = 0; i < 100000; i++) {
      int m = its;
      its += 1;
      double miss = misses[imiss];
      double phi = phis[random.nextInt(phis.length)];
      
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
      String vName = v.toString().replace(" ", "").replace(',', ' ');
            
      Logger.info("Executing test %d: %d items (%d of %d pairs), phi = %.1f, miss = %.1f", i+1, m, v.size(), r.size(), phi, miss);

      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      expander.setTimeout(20 * Utils.ONE_MINUTE);
      Double pExpander;
      boolean next = false;
      try { 
        pExpander = expander.getProbability(v);
        pExpander = Math.log(pExpander);
        if (pExpander.isInfinite()) {
          Logger.info("Expander infinite. Skipping to next miss rate");
          next = true;
        }
      }
      catch (TimeoutException te) { 
        Logger.info("Expander timeout. Skipping to next miss rate");
        pExpander = Double.NaN;
        next = true;        
      }
      if (next) {
        its = 10;
        imiss = (imiss + 1) % misses.length;
        continue;
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec: %f", timeExpander / 1000, pExpander);
            
      
      String line = String.format("%d,%.1f,%.1f,%s,%d", m, phi, miss, vName, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", pExpander, timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      
      
      // AMP
      double[] times = { 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2 }; 
      long startAmp = System.currentTimeMillis();

      long tt = 0;
      AMPInferator ampInferator = new AMPInferator(model, v);
      for (int k = 0; k < times.length; k++) {
        long time = Math.round(times[k] * timeExpander);
        long dt = time - tt;
        tt = time;
        double pAmp = ampInferator.sampleMillis(dt);
        pAmp = Math.log(pAmp);
        long tAmp = System.currentTimeMillis() - startAmp;
        Logger.info("%d ms of AMP: %f", tAmp, pAmp);
        line += String.format(",%d,%f,%d,%d", ampInferator.getCount(), pAmp, time, tAmp);
      }
      
      
      // SampleSearch
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      
      line += String.format(",%d,%d", timeGM, gm.getNetworkSize());
      
      SampleSearchInferator ss = new SampleSearchInferator(gm);
      long[] timess = { timeExpander / 5 - timeGM, timeExpander / 2 - timeGM, timeExpander - timeGM};
      for (long time: timess) {
        if (time < 0) {
          Logger.info("SampleSearch skipped (time = %d ms)", time);
          line += String.format(",%f,%f,%d", 0.001d * time, Float.NaN, -1);
          continue;
        }
        int t = (int) Math.round(1d * time / 1000);
        t = Math.max(t, 1);
        Double pss = ss.exec(t);
        long timeSS = ss.getTime();
        Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, t, pss);
        line += String.format(",%d,%f,%d", t, pss, timeSS);
      }
      
      out.println(line);
      out.flush();
      
    }
    out.close();
  }
  
  /** Random preference pairs, now with Jayes */
  public static void five(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    // int[] its = { 10, 15, 20, 25, 30 };
    double[] phis = { 0.8 };
    double[] misses = { 0.8, 0.5, 0.2 };
    
    int its = 10;
    int imiss = 0;
    
    for (int i = 0; i < 100000; i++) {
      int m = its;
      its += 2;
      double miss = misses[imiss];
      double phi = phis[random.nextInt(phis.length)];
      
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
      String vName = v.toString().replace(" ", "").replace(',', ' ');
            
      Logger.info("Executing test %d: %d items (%d of %d pairs), phi = %.1f, miss = %.1f", i+1, m, v.size(), r.size(), phi, miss);

      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      expander.setTimeout(20 * Utils.ONE_MINUTE);
      Double pExpander;
      boolean next = false;
      try { 
        pExpander = expander.getProbability(v);
        pExpander = Math.log(pExpander);
        if (pExpander.isInfinite()) {
          Logger.info("Expander infinite. Skipping to next miss rate");
          next = true;
        }
      }
      catch (TimeoutException te) { 
        Logger.info("Expander timeout. Skipping to next miss rate");
        pExpander = Double.NaN;
        next = true;        
      }
      if (next) {
        its = 10;
        imiss = (imiss + 1) % misses.length;
        continue;
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec: %f", timeExpander / 1000, pExpander);
            
      
      String line = String.format("%d,%.1f,%.1f,%s,%d", m, phi, miss, vName, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", pExpander, timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      
      
      // AMP
      double[] times = { 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2 }; 
      long startAmp = System.currentTimeMillis();

      long tt = 0;
      AMPInferator ampInferator = new AMPInferator(model, v);
      for (int k = 0; k < times.length; k++) {
        long time = Math.round(times[k] * timeExpander);
        long dt = time - tt;
        tt = time;
        double pAmp = ampInferator.sampleMillis(dt);
        pAmp = Math.log(pAmp);
        long tAmp = System.currentTimeMillis() - startAmp;
        Logger.info("%d ms of AMP: %f", tAmp, pAmp);
        line += String.format(",%d,%f,%d,%d", ampInferator.getCount(), pAmp, time, tAmp);
      }
      
      
      // GM
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d sec", timeGM / 1000);
      
      line += String.format(",%d,%d", timeGM, gm.getNetworkSize());
      
      
      // Jayes
      long startJayes = System.currentTimeMillis();
      double pJayes = -1;
      int treeWidth = -1;
      long timeJayes;
      long jayesResult = 0;
      try {
        JayesInferator jayesInferator = new JayesInferator(gm);
        pJayes = jayesInferator.getProbability();
        treeWidth = jayesInferator.getJunctionTreeWidth();
        timeJayes = System.currentTimeMillis() - startJayes;
        pJayes = Math.log(pJayes);
        Logger.info("Jayes done in %d sec: %f", timeJayes / 1000, pJayes);
      }
      catch (Throwable t) {
        timeJayes = System.currentTimeMillis() - startJayes;
        pJayes = Math.log(pJayes);
        Logger.info("Jayes failed in %d sec", timeJayes / 1000);
        if (t instanceof ArrayIndexOutOfBoundsException) jayesResult = -2;
        else if (t instanceof OutOfMemoryError) jayesResult = -1;
      }
      line += String.format(",%d,%f,%d,%d", treeWidth, pJayes, timeJayes, jayesResult);
      line += String.format(",%d,%d,%d", Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory(), Runtime.getRuntime().maxMemory());
      
      // SampleSearch
      SampleSearchInferator ss = new SampleSearchInferator(gm);
      long[] timess = { timeExpander / 5 - timeGM, timeExpander / 2 - timeGM, timeExpander - timeGM};
      for (long time: timess) {
        if (time < 0) {
          Logger.info("SampleSearch skipped (time = %d ms)", time);
          line += String.format(",%f,%f,%d", 0.001d * time, Float.NaN, -1);
          continue;
        }
        int t = (int) Math.round(1d * time / 1000);
        t = Math.max(t, 1);
        Double pss = ss.exec(t);
        long timeSS = ss.getTime();
        Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, t, pss);
        line += String.format(",%d,%f,%d", t, pss, timeSS);
      }
      
      out.println(line);
      out.flush();
      
    }
    out.close();
  }
  
  /** Random preference pairs, now with DynamicPreferenceExpander, too */
  public static void six(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    // int[] its = { 10, 15, 20, 25, 30 };
    double[] phis = { 0.8 }; // 0.8
    double[] misses = { 0.5, 0.8, 0.2 };
    
    int its = 10;
    int imiss = 0;
    
    for (int i = 0; i < 100000; i++) {
      int m = its;
      its += 2;
      double miss = misses[imiss];
      double phi = phis[random.nextInt(phis.length)];
      
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
      String vName = v.toString().replace(" ", "").replace(',', ' ');
            
      Logger.info("Executing test %d: %d items (%d of %d pairs), phi = %.1f, miss = %.1f", i+1, m, v.size(), r.size(), phi, miss);

      
      // EXPANDER
      long timeout = 10 * Utils.ONE_MINUTE;
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      expander.setTimeout(timeout);
      Double pExpander;
      boolean next = false;
      try { 
        pExpander = expander.getProbability(v);
        pExpander = Math.log(pExpander);
        if (pExpander.isInfinite()) {
          Logger.info("Expander infinite. Skipping to next miss rate");
          next = true;
        }
      }
      catch (TimeoutException te) { 
        Logger.info("Expander timeout. Skipping to next miss rate");
        pExpander = Double.NaN;
        next = true;        
      }
      if (next) {
        its = 10;
        imiss = (imiss + 1) % misses.length;
        continue;
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec: %f", timeExpander / 1000, pExpander);
      
      String line = String.format("%d,%.1f,%.1f,%s,%d", m, phi, miss, vName, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", pExpander, timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      
      
      // DYNAMIC EXPANDER
      long startd = System.currentTimeMillis();
      DynamicPreferenceExpander dExpander = new DynamicPreferenceExpander(model);
      dExpander.setTimeout(timeout);
      Double pdExpander;
      next = false;
      try { 
        pdExpander = dExpander.getProbability(v);
        pdExpander = Math.log(pdExpander);
        if (pdExpander.isInfinite()) {
          Logger.info("Expander infinite. Skipping to next miss rate");
          next = true;
        }
      }
      catch (TimeoutException te) { 
        Logger.info("Expander timeout. Skipping to next miss rate");
        pdExpander = Double.NaN;
        next = true;        
      }
      if (next) {
        its = 10;
        imiss = (imiss + 1) % misses.length;
        continue;
      }
      long timedExpander = System.currentTimeMillis() - startd;
      Logger.info("DynamicPreferenceExpander done in %d sec: %f", timedExpander / 1000, pdExpander);
      
      line += String.format(",%f,%d", pdExpander, timedExpander);
      
      /*
      // AMP
      double[] times = { 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2 }; 
      long startAmp = System.currentTimeMillis();

      long tt = 0;
      AMPInferator ampInferator = new AMPInferator(model, v);
      for (int k = 0; k < times.length; k++) {
        long time = Math.round(times[k] * timeExpander);
        long dt = time - tt;
        tt = time;
        double pAmp = ampInferator.sample(dt);
        pAmp = Math.log(pAmp);
        long tAmp = System.currentTimeMillis() - startAmp;
        Logger.info("%d ms of AMP: %f", tAmp, pAmp);
        line += String.format(",%d,%f,%d,%d", ampInferator.getCount(), pAmp, time, tAmp);
      }
      
      
      // SampleSearch
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      
      line += String.format(",%d,%d", timeGM, gm.getNetworkSize());
      
      SampleSearchInferator ss = new SampleSearchInferator(gm);
      long[] timess = { timeExpander / 5 - timeGM, timeExpander / 2 - timeGM, timeExpander - timeGM};
      for (long time: timess) {
        if (time < 0) {
          Logger.info("SampleSearch skipped (time = %d ms)", time);
          line += String.format(",%f,%f,%d", 0.001d * time, Float.NaN, -1);
          continue;
        }
        int t = (int) Math.round(1d * time / 1000);
        t = Math.max(t, 1);
        Double pss = ss.exec(t);
        long timeSS = ss.getTime();
        Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, t, pss);
        line += String.format(",%d,%f,%d", t, pss, timeSS);
      }
      */
      
      out.println(line);
      out.flush();
      
    }
    out.close();
  }
}
